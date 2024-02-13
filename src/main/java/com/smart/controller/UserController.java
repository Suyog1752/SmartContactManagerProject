package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.aspectj.bridge.Message;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;

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
	public String processContact(@Valid

	@ModelAttribute("contact") Contact contact, BindingResult result1, @RequestParam("profileImage") MultipartFile file, // MultipartFile
																															// we
																															// use
																															// to
																															// get
																															// uploded
																															// file
																															// in
																															// controller
																															// for
																															// use
			Principal principal, HttpSession session, Model model) {
		if (result1.hasErrors()) {
			// Log or print validation errors for debugging
			System.out.println("Validation errors: " + result1.getAllErrors());
			model.addAttribute("contact", contact);
			return "normal/add_contact_form";
		}

		try {

			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);

			// processing and uploading file...
			if (file.isEmpty()) {
				// if the file is empty, set a default image
				contact.setImage("contact.png");
			} else {
				// fill the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());

				File saveFile = new ClassPathResource("static/IMG").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}

			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);

			System.out.println("Data " + contact);
			System.out.println("Addded to Database");

			// message success.....
			session.setAttribute("message",
					new com.smart.helper.Message("Your Contact is Added !!, And More...", "success"));

			model.addAttribute("contact", new Contact());
			return "normal/add_contact_form";

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			// error message......
			session.setAttribute("message",
					new com.smart.helper.Message("Somethig Went Wrong,Try Again... ", "danger"));

			return "normal/add_contact_form";
		}
	}

	// show contact h handler..
	// per page=5[n]
	// current page=0[page]
	@GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show Contact");

		// send the co;ntact list from here
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		int id = user.getId();
		// creating object of pageable
		// current page-page
		// contact per page-5
		Pageable pageable = PageRequest.of(page, 5);
		Page<Contact> contacts = this.contactRepository.findContactByUser(id, pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show-contact";
	}

	// contact_detail
	// showing particular contact details
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid, Model model, Principal principal) {
		System.out.println("CID" + cid);
		Optional<Contact> conatctOptional = this.contactRepository.findById(cid);
		Contact contact = conatctOptional.get();

		// to avoiding security bug,check is this user accessing its own contacts not an
		// others contacts
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if (contact.getUser().getId() == user.getId()) {
			model.addAttribute("title", contact.getName() + " " + contact.getSecondName());
			model.addAttribute("contact", contact);

		}

		return "normal/contact_detail";
	}

	// delete contact handler
	@GetMapping("/delete/{cid}/{page}")
	public String deleteContact(@PathVariable("cid") Integer cid, @PathVariable("page") Integer page, Model model,
			Principal principal, HttpSession session) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.orElse(null);

		// Check if the contact exists
		if (contact != null) {
			String userName = principal.getName();
			User user = this.userRepository.getUserByUserName(userName);

			// Check if the logged-in user is the owner of the contact
			if (contact.getUser().getId() == user.getId()) {
				// Delete the image file
				try {
					if (contact.getImage() != null && !contact.getImage().isEmpty()) {
						File deleteFile = new ClassPathResource("static/IMG").getFile();
						Path path = Paths.get(deleteFile.getAbsolutePath() + File.separator + contact.getImage());
						Files.deleteIfExists(path);
						System.out.println("Image file deleted: " + path.toString());
					}
				} catch (IOException e) {
					e.printStackTrace();
					// Handle exception as needed
				}

				// Delete the contact
				this.contactRepository.delete(contact);
				session.setAttribute("message",
						new com.smart.helper.Message("Contact deleted successfully", "success"));
			}
		}

		return "redirect:/user/show-contact/" + page;
	}

	// open update form handler
	@PostMapping("update-contact/{cid}")
	public String updateForm(Model model, @PathVariable("cid") Integer cid) {
		model.addAttribute("title", "Update Contact");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// process-update handler
	@PostMapping("/process-update")
    public String updateHadler(@ModelAttribute Contact contact,
    		@RequestParam("profileImage") MultipartFile file,
    		Model model,HttpSession session,Principal principal)
    		
    		 
    
    {
		try {

			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();

			// image..
			if (!file.isEmpty()) {
				// file .. work
				// rewrite

				// delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				// update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetail.getImage());
			}

			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);
			session.setAttribute("message", new com.smart.helper.Message("Your contact is updated...", "success"));
		}

		catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("contact name =" + contact.getName());
		System.out.println("contact id =" + contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
	
	//open setting handler
	@GetMapping("/settings")
		public String openSetting() {
			return "normal/settings";
		}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldpassword")String oldPassword,
			@RequestParam("newpassword")String newPassword,
			Principal principal,
			HttpSession session
			
			) {
		
		System.out.println("OLD PASSWORD:"+oldPassword);
		System.out.println("NEW PASSWORD:"+newPassword);
		
		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());
		
		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//change the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));

			this.userRepository.save(currentUser);
			session.setAttribute("message", new com.smart.helper.Message("Your password is changed Successfully !!", "success"));
			
			
			
		}else {
			//error..
			session.setAttribute("message", new com.smart.helper.Message("please Enter correct old password !!", "danger"));
			return "redirect:/user/settings";
		}
		return "redirect:/user/index";
		
		
	}
	
	//creating order for payment
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
	    int amt = Integer.parseInt(data.get("amount").toString());

	    var client = new RazorpayClient("rzp_test_VKLrVJm4KS74R1", "BlubrBpwg8kaxyQUiIaNyyOA");

	    JSONObject ob = new JSONObject();
	    ob.put("amount", amt * 100);
	    ob.put("currency", "INR");
	    ob.put("receipt", "txn_234532");

	    Order order = client.orders.create(ob);

	    return order.toString();
	}

		
	
    }




