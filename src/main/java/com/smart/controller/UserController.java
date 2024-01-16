package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;

	// method for adding common data to responce
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();

		// get the user data by usign this username
		User user = userRepository.getUserByUserName(username);

		model.addAttribute("user", user);
	}

	// home_dashbpard
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		String username = principal.getName();

		// get the user data by usign this username
		User user = userRepository.getUserByUserName(username);
//		System.out.println("USER " + user);

		model.addAttribute("user", user);
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// processing add contact form
	@PostMapping("/process-contact")
	public String processContact(
	    @Valid @ModelAttribute("contact") Contact contact,
	     BindingResult result1,
	    @RequestParam("profileImage")MultipartFile file, // MultipartFile we use to get uploded file in controller for use
	    Principal principal,
	    Model model
	) {   
		if (result1.hasErrors()) {
	        // Log or print validation errors for debugging
	        System.out.println("Validation errors: " + result1.getAllErrors());
	        model.addAttribute("contact", contact);
	        return "normal/add_contact_form";
	    }
	    
		

		try {
			
			//updated to
			
			String name = principal.getName(); 
			User user = this.userRepository.getUserByUserName(name);

			//processing and uploding file...
			if (file.isEmpty()) {
				//if the fiel is empty give message
				System.out.println("Image is empty");
				
			}else {
				//fill the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				
			File saveFile=new ClassPathResource("static/IMG").getFile();
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(),path , StandardCopyOption.REPLACE_EXISTING);
			System.out.println("img is uploded");
			}
			
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("Data " + contact);
			System.out.println("Addded to Database");
			model.addAttribute("contact", new Contact());
			return "normal/add_contact_form";

		} catch (Exception e) {
	        e.printStackTrace();
	        model.addAttribute("contact", contact);
	        return "normal/add_contact_form";
	    }

	}
}
