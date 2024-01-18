package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.aspectj.bridge.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;


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

			@ModelAttribute("contact") Contact contact, 
			BindingResult result1,
			@RequestParam("profileImage") MultipartFile file, // MultipartFile we use to get uploded file in controller
																// for use
			Principal principal,
			HttpSession session,
			Model model) {
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
			
			//message success.....
			session.setAttribute("message", new com.smart.helper.Message("Your Contact is Added !!, And More...", "success"));
			
			
			model.addAttribute("contact", new Contact());
			return "normal/add_contact_form";
			

		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("contact", contact);
			//error message......
			session.setAttribute("message", new com.smart.helper.Message("Somethig Went Wrong,Try Again... ", "danger"));
			
			return "normal/add_contact_form";
		}
	}
	        
	
	// show contact h handler..
	//per page=5[n]
	//current page=0[page]
    @GetMapping("/show-contact/{page}")
	public String showContact(@PathVariable("page") Integer page  ,Model model,Principal principal) {
    	model.addAttribute("title", "Show Contact");
    	
		// send the co;ntact list from here
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		int id = user.getId();
		//creating object of  pageable
		//current page-page
		//contact per page-5
		 Pageable pageable= PageRequest.of(page, 5); 	
		Page<Contact> contacts = this.contactRepository.findContactByUser(id,pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		
		
		
		return "normal/show-contact";
	}


    //showing particular contact details
    @RequestMapping("/{cid}/contact")
    public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
    	System.out.println("CID"+cid);
    	Optional<Contact> conatctOptional = this.contactRepository.findById(cid);
    	Contact contact = conatctOptional.get();
    	
    	//to avoiding security bug,check is this user accessing its own contacts not an others contacts
    	String userName = principal.getName();
    	User user = this.userRepository.getUserByUserName(userName);
    	if(contact.getUser().getId()==user.getId())
    	{
    		model.addAttribute("title", contact.getName()+" "+contact.getSecondName());
    		model.addAttribute("contact", contact);
    	}
    	
    	
    	return "normal/contact_detail";
    }

    //delete contact handler
    @GetMapping("/delete/{cid}/{page}")
    public String deleteContact(
            @PathVariable("cid") Integer cid,
            @PathVariable("page") Integer page,
            Model model,
            Principal principal,
            HttpSession session
    ) {
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
                session.setAttribute("message", new com.smart.helper.Message("Contact deleted successfully", "success"));
            }
        }

        return "redirect:/user/show-contact/" + page;
    }

    
    
}

