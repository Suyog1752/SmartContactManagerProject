package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository userRepository;
	
	Random random = new Random(1000);
	//email id form open handler
	@RequestMapping(value = "/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

//email id form open handler
@PostMapping("/send-otp")
public String sendOTP(@RequestParam("email") String email,HttpSession session) {
	System.out.println("email :"+email);
	//genrating opt of 4 digit
	
	int otp = random.nextInt(999999);
	
	System.out.println("OTP :"+otp);
	
	//write code for send otp to email...
	
	String subject="OTP from smart contact manager";
	String message=""
			       +"<div style='border:1px solid #e2e2e2; padding:20px'>" 
			       
			       +"OTP IS"
			       +"<h1>"
			       +"<b>"+otp
			       +"</n>"
			       +"</h1>" 
			       +"</div>" ;
	String to=email;
	String from="informalwork1752@gmail.com";
boolean flag=this.emailService.sendEmail(subject, message, to, from);
	
if (flag) {
	session.setAttribute("myotp", otp);
	session.setAttribute("email", email);
	return "verify_otp"; 
} else {
	session.setAttribute("message", "Enter your correct email id !!"); 
	return "forgot_email_form"; 
}

	
}

//verify-otp handler
@PostMapping("/verify-otp")
public String verifyOtp(@RequestParam("otp") int otp,HttpSession session) {
	int myotp=(int)session.getAttribute("myotp");
	String email=(String) session.getAttribute("email");
	
	//verifying both backend and user input otp
	if (myotp==otp) {
		//password change form
		
		User user = this.userRepository.getUserByUserName(email);
		if (user==null) {
			//send the error message 
			session.setAttribute("message", "User does not exist with this enail !!"); 
			return "forgot_email_form"; 
			
		}else {
			//send the password change form
			
		}
		return "password_change_form";
	}else {
		
		session.setAttribute("message", "You have entered wrong otp !!");
	return "verify_otp";
	}
	
}
}
