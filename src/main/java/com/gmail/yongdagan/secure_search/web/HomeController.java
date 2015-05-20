package com.gmail.yongdagan.secure_search.web;

import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.gmail.yongdagan.secure_search.service.AccountManager;
import com.gmail.yongdagan.secure_search.service.IndexManager;
import com.gmail.yongdagan.secure_search.service.ServiceException;

@Controller
public class HomeController {
	
	@Autowired
	private AccountManager accountManager;
	@Autowired
	private IndexManager indexManager;
	
	private Logger logger = LogManager.getLogger(HomeController.class.getName());
	
	@RequestMapping(value={"/", "/index", "/index.htm"}, method=RequestMethod.GET)
	public String showHomePage(HttpSession session) {
		if(session.getAttribute("account") == null){
			return "index";
		}
		return "redirect:/search";
	}
	
	@RequestMapping(value={"/login"}, method=RequestMethod.GET)
	public String showLoginPage(HttpSession session, Model model) {
		if(session.getAttribute("account") == null){
			model.addAttribute("login", true);
			return "login";
		}
		return "redirect:/search";
	}
	
	@RequestMapping(value={"/register"}, method=RequestMethod.GET)
	public String showRegisterPage(HttpSession session, Model model) {
		if(session.getAttribute("account") == null){
			return "login";
		}
		return "redirect:/search";
	}
	
	@RequestMapping(value="/login", method=RequestMethod.POST)
	public String login(@RequestParam(value="username") String username,
			@RequestParam(value="password") String password,
			HttpSession session, Model model) {
		Account account = null;
		try {
			account = accountManager.login(username, password);
		} catch (ServiceException e) {
			model.addAttribute("login", true);
			model.addAttribute("error", "登录失败");
			logger.error(username + ": login fail", e);;
			return "login";
		}
		if(account == null) {
			model.addAttribute("login", true);
			model.addAttribute("error", "用户名或密码错误");
			logger.info(username + ": login fail");
			return "login";
		} else {
			session.setAttribute("account", account);
			logger.info(username + ": login success");
			return "redirect:/search";
		}
	}
	
	@RequestMapping(value="/register", method=RequestMethod.POST)
	public String register(@RequestParam(value="username") String username,
			@RequestParam(value="password") String password,
			HttpSession session, Model model) {
		if(password.length() < 8) {
			model.addAttribute("error", "密码长度不能小于8!");
			return "login";
		}
		Account account = null;
		try {
			account = accountManager.register(username, password);
			logger.info(username + ": register");
		} catch (ServiceException e) {
			model.addAttribute("error", "注册失败");
			logger.error("register fail", e);
			return "login";
		}
		if(account == null) {
			model.addAttribute("error", "此用户名已被注册");
			return "login";
		} else {
			session.setAttribute("account", account);
			return "redirect:/search";
		}
	}
	
	@RequestMapping(value={"/download"})
	public ResponseEntity<byte[]> downloadSecureManager(HttpServletRequest request) {
		String filename = "helper.jar";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", filename);
		try {
			return new ResponseEntity<byte[]>(FileUtils.readFileToByteArray(Paths.get(
					request.getServletContext().getRealPath("/"), "WEB-INF", "views", "helper.jar").toFile()),
					headers, HttpStatus.CREATED);
		} catch (IOException e) {
			logger.error("helper.jar is missing", e);
		}
		return null;
	}
	
	@RequestMapping(value={"/help"})
	public String showHelpPage(HttpSession session, Model model) {
		boolean login = session.getAttribute("account") != null;
		model.addAttribute("login", login);
		return "help";
	}
	
	@RequestMapping(value={"/helpAttachment"})
	public String showAttachmentHelpPage() {
		return "helpAttachment";
	}
	
}
