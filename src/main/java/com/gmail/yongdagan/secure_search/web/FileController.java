package com.gmail.yongdagan.secure_search.web;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;
import com.gmail.yongdagan.secure_search.persist.dataobject.CryptoUtil;
import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;
import com.gmail.yongdagan.secure_search.service.AccountManager;
import com.gmail.yongdagan.secure_search.service.IndexManager;
import com.gmail.yongdagan.secure_search.service.ServiceException;

@Controller
public class FileController {
	
	public static final long MAX_CAPACITY = 1024 * 1024 * 1024;
	
	@Autowired
	private IndexManager indexManager;
	@Autowired
	private AccountManager accountManager;
	
	@RequestMapping(value="/upload", method=RequestMethod.POST)
	public String uploadFiles(HttpSession session, Model model,
			@RequestParam(value="files", required=true) MultipartFile[] files) {
		Account account = (Account) session.getAttribute("account");
		if (account == null) {
			return "redirect:index";
		}
		// upload files
		if (files.length != 0) {
			// check account capacity
			Long uploadSize = 0L;
			for(MultipartFile file : files) {
				// uncheck index
				if (!file.isEmpty() && !file.getOriginalFilename().equals("indexFile.txt")) {
					uploadSize += file.getSize();
				}
			}
			
			// enough capacity
			if(uploadSize + account.getCapacity() <= MAX_CAPACITY) {
				Path accountPath = Paths.get(session.getServletContext()
						.getRealPath("/"), "WEB-INF", "userFiles", Long
						.toString(account.getId()));
				try {
					// create account directory if not exists
					try {
						Files.createDirectory(accountPath);
					} catch (FileAlreadyExistsException e) {
						// ignore
					}
					// parse index
					Long accountId = account.getId();
					HashMap<String, Long> docs = new HashMap<String, Long>();
					for (MultipartFile file : files) {
						if(file.isEmpty()) continue;
						if (file.getOriginalFilename().equals("indexFile.txt")) {
							// load and check docIds
							InputStream indexStream = file.getInputStream();
							try(BufferedReader reader = new BufferedReader(new InputStreamReader(indexStream))) {
								// read doc
								String tmp = null;
								int k = 1;
								while(!(tmp = reader.readLine()).equals("")) {
									String[] str = tmp.split(" ");
									Long docId = Long.valueOf(str[0]);
									StringBuffer docName = new StringBuffer();
									for(int i = 1; i < str.length; i ++) {
										docName.append(str[i] + " ");
									}
									
									Doc doc = new Doc();
									doc.setAccountId(accountId);
									doc.setDocId(docId);
									doc.setName(docName.substring(0, docName.length() - 1));
									if(k == 1) {
										k = 0;
										if(indexManager.hasDoc(accountId, docId)) {
											String error = CryptoUtil.encodeBASE64("生成索引时初始docId设置不当".getBytes());
											return "redirect:/manage?error=" + error;
										}
									}
									indexManager.addDoc(doc);
									docs.put(doc.getName(), docId);
								}
							}
							Thread thread = new Thread(new UploadRunnable(account.getId(), file.getInputStream(), accountPath));
							thread.start();
						}
					}
					// save files
					for(MultipartFile file : files) {
						if(file.isEmpty()) continue;
						String fileName = new String(file.getOriginalFilename().getBytes("ISO8859-1"), "UTF-8");
						if (!fileName.equals("indexFile.txt")) {
							Path filePath = accountPath.resolve(docs.get(fileName.substring(0, fileName.lastIndexOf(".")))
									+ fileName);
							file.transferTo(filePath.toFile());
						}
					}
					// update capacity
					accountManager.updateCapacity(account, account.getCapacity() + uploadSize);
				} catch (Exception e) {
					e.printStackTrace();
					model.addAttribute("error", "system error");
				}
			}
		}
		return "redirect:/manage";
	}
	
	@RequestMapping(value="/manage")
	public String allFiles(HttpSession session, Model model,
			@RequestParam(value="page", required=false) Integer page,
			@RequestParam(value="error", required=false) String error) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		if(error != null) {
			error = new String(CryptoUtil.decodeBASE64(error));
			model.addAttribute("error", error);
		}
		if(page == null || page.intValue() < 1) {
			page = 1;
		}
		try {
			int startId = (int) indexManager.getNextStartIdByAccountId(account.getId());
			int pageNum = (int) (indexManager.getDocNumByAccountId(account.getId()) - 1) / 10 + 1;
			if(page > pageNum) {
				page = pageNum;
			}
			List<Doc> docs = indexManager.getAccountFiles(account.getId(), page);
			model.addAttribute("docs", docs);
			model.addAttribute("username", account.getUsername());
			model.addAttribute("capacity", (int)((double)account.getCapacity() / 1024 / 1024 * 1000) / 1000.0);
			model.addAttribute("totalCapacity", (int)((double)MAX_CAPACITY / 1024 / 1024 * 1000) / 1000.0);
			model.addAttribute("pageNum", pageNum);
			model.addAttribute("page", page);
			model.addAttribute("startId", startId);
		} catch (ServiceException e) {
			e.printStackTrace();
			model.addAttribute("error", "system error");
		}
		return "manage";
	}
	
	@RequestMapping(value="/delete")
	public String deleteFile(HttpSession session, Model model,
			@RequestParam(value="id", required=true) Long docId,
			@RequestParam(value="name", required=true) String docName,
			@RequestParam(value="page", required=false) String page) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		try {
			Long accountId = account.getId();
			int n = indexManager.deleteDoc(accountId, docId);
			if(n == 1) {
				// delete file
				Path file = Paths.get(session.getServletContext().getRealPath("/"),
						"WEB-INF", "userFiles", Long.toString(accountId), Long.toString(docId) + docName+".dat");
				long size = file.toFile().length();
				if(Files.deleteIfExists(file)) {
					accountManager.updateCapacity(account, account.getCapacity() - size);
				}
			}
		} catch (Exception e) {
			model.addAttribute("error", "系统出错。请重试！");
		}
		if(page == null) {
			return "redirect:/manage";
		} else {
			return "redirect:/manage?page=" + page;
		}
	}
	
	@RequestMapping(value="/clearAccountFiles")
	public String clearFiles(HttpSession session, Model model) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		try {
			Path accountPath = Paths.get(session.getServletContext().getRealPath("/"),
					"WEB-INF", "userFiles", Long.toString(account.getId()));
			indexManager.clearIndexAndFiles(account.getId(), accountPath);
			accountManager.updateCapacity(account, 0L);
		} catch (ServiceException e) {
			model.addAttribute("error", "系统出错。请重试！");
		}
		return "redirect:/manage";
	}
	
	private class UploadRunnable implements Runnable {
		
		private Long accountId;
		private InputStream inputStream;
		private Path accountPath;
		
		public UploadRunnable(Long accountId, InputStream inputStream, Path accountPath) {
			this.accountId = accountId;
			this.inputStream = inputStream;
			this.accountPath = accountPath;
		}

		@Override
		public void run() {
			try {
				indexManager.parseIndexFile(accountId, inputStream, accountPath);
			} catch (ServiceException e) {
				e.printStackTrace();
			}
		}
		
	}

}
