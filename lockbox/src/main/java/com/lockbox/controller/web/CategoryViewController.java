package com.lockbox.controller.web;

import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.CategoryCreationDto;
import com.lockbox.mapper.CategoryMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryViewController {

    private final CategoryService categoryService;
    private final UserService userService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryViewController(CategoryService categoryService, UserService userService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.userService = userService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    public String createCategory(@ModelAttribute CategoryCreationDto categoryCreationDto, 
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest request) {
        try {
            // Log request info for debugging
            System.out.println("Creating category: " + categoryCreationDto.getName());
            User user = userService.findById(categoryCreationDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (categoryService.existsByUserIdAndName(user.getId(), categoryCreationDto.getName())) {
                redirectAttributes.addFlashAttribute("error", "A category with this name already exists");
                return getRedirectUrl(request);
            }

            Category category = categoryMapper.toEntity(categoryCreationDto);
            category.setUser(user);
            categoryService.save(category);

            redirectAttributes.addFlashAttribute("success", "Category created successfully");
            return getRedirectUrl(request);
        } catch (Exception e) {
            e.printStackTrace(); // Add stack trace for easier debugging
            redirectAttributes.addFlashAttribute("error", "Failed to create category: " + e.getMessage());
            return getRedirectUrl(request);
        }
    }

    private String getRedirectUrl(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer.substring(referer.indexOf("/", 8));
        }
        return "redirect:/";
    }

    /**
     * Alternative endpoint that accepts JSON for AJAX requests
     */
    @PostMapping(value = "/api", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> createCategoryJson(@Valid @RequestBody CategoryCreationDto categoryCreationDto, 
                                             HttpServletRequest request) {
        try {
            System.out.println("Creating category via JSON API: " + categoryCreationDto.getName());
            User user = userService.findById(categoryCreationDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (categoryService.existsByUserIdAndName(user.getId(), categoryCreationDto.getName())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A category with this name already exists");
            }

            Category category = categoryMapper.toEntity(categoryCreationDto);
            category.setUser(user);
            Category savedCategory = categoryService.save(category);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryMapper.toDto(savedCategory));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to create category: " + e.getMessage());
        }
    }
} 