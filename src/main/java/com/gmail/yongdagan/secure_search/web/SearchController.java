package com.gmail.yongdagan.secure_search.web;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.gmail.yongdagan.secure_search.persist.dataobject.Account;
import com.gmail.yongdagan.secure_search.persist.dataobject.Doc;
import com.gmail.yongdagan.secure_search.service.AccountManager;
import com.gmail.yongdagan.secure_search.service.IndexManager;
import com.gmail.yongdagan.secure_search.service.ServiceException;

@Controller
public class SearchController {
	
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private IndexManager indexManager;
	
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public String showSearchPage(HttpSession session, Model model) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		model.addAttribute("account", account);
		return "search";
	}
	
	@RequestMapping(value="/quit")
	public String showHomePage(HttpSession session) {
		session.removeAttribute("account");
		return "redirect:/index";
	}
	
	@RequestMapping(value="/retrieval", method=RequestMethod.POST)
	public String searchFiles(HttpSession session, Model model,
			@RequestParam(value="searchType", required=true) String searchType,
			@RequestParam(value="hmac", required=true) String hmac,
			@RequestParam(value="trapdoor", required=true) String trapdoor) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		// check addKey
		if(account.getAddKey().equals("")) {
			model.addAttribute("error", "搜索前请在个人信息处上传服务器密钥！");
		} else {
			// search
			List<Doc> docs = null;
			if(searchType.equals("boolean")) {
				try {
					docs = indexManager.booleanSearch(account.getId(), hmac, trapdoor);
					model.addAttribute("searchType", "boolean");
				} catch (ServiceException e) {
					e.printStackTrace();
					model.addAttribute("error", "系统出错。请重试！");
				}
			} else {
				try {
					docs = indexManager.rankSearch(account.getId(), account.getAddKey(), hmac, trapdoor);
					model.addAttribute("searchType", "rank");
				} catch (ServiceException e) {
					e.printStackTrace();
					model.addAttribute("error", "系统出错。请重试！");
				}
			}
			model.addAttribute("docs", docs);
		}
		return "retrieval";
	}
	
	
	@RequestMapping(value="/info", method=RequestMethod.GET)
	public String showAccountInfo(HttpSession session, Model model) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		model.addAttribute("username", account.getUsername());
		model.addAttribute("addKey", account.getAddKey());
		return "info";
	}
	
	@RequestMapping(value="/info", method=RequestMethod.POST)
	public String updateAccountInfo(HttpSession session, Model model,
			@RequestParam(value="oldPassword", required=true) String oldPassword,
			@RequestParam(value="newPassword") String newPassword,
			@RequestParam(value="addKey") String addKey) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return "redirect:index";
		}
		if(newPassword.length() != 0 && newPassword.length() < 8) {
			model.addAttribute("tip", "新密码长度不能小于8!");
			model.addAttribute("good", "bad");
		}
		else {
			try {
				if(accountManager.updateInfo(account, oldPassword, newPassword,
					addKey)) {
					model.addAttribute("tip", "更新成功!");
					model.addAttribute("good", "good");
				} else {
					model.addAttribute("tip", "密码错误!");
					model.addAttribute("good", "bad");
				}
			} catch (ServiceException e) {
				model.addAttribute("error", "系统出错。请重试！");
			}
		}
		model.addAttribute("username", account.getUsername());
		model.addAttribute("addKey", account.getAddKey());
		return "info";
	}
	
	@RequestMapping(value="/getFile")
	public ResponseEntity<byte[]> getFile(HttpSession session, Model model,
			@RequestParam(value="name", required=true) String name,
			@RequestParam(value="id", required=true) String id) {
		Account account = (Account) session.getAttribute("account");
		if(account == null) {
			return null;
		}
		String filename = id + name + ".dat";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		try {
			name = new String(name.getBytes("UTF-8"), "ISO8859-1");
		} catch (UnsupportedEncodingException e1) {
			// ignore
			e1.printStackTrace();
		}
		headers.setContentDispositionFormData("attachment", name + ".dat");
		try {
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(Paths.get(
					session.getServletContext().getRealPath("/"), "WEB-INF", "userFiles",
					Long.toString(account.getId()), filename).toFile()), headers, HttpStatus.CREATED);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
