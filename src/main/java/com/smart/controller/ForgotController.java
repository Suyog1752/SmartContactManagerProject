package com.smart.controller;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.constraints.Email;

@Controller
public class ForgotController {
	Random random = new Random(10000);

	//email id form open handler
	@RequestMapping(value = "/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

//email id form open handler
@PostMapping("/send-otp")
public String sendOTP(@RequestParam("email") String email) {
	System.out.println("email"+email);
	//genrating opt of 4 digit
	
	int otp = random.nextInt(10000);
	
	System.out.println("OTP "+otp);
	
	//write code for send otp to email...
	return "verify_otp";
}
}
