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
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

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

    @GetMapping
    public String listCategories(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Category> categories = categoryService.findByUserId(user.getId());
        Map<Long, Long> passwordCounts = categoryService.getPasswordCountsByCategory(user.getId());
        
        model.addAttribute("categories", categories);
        model.addAttribute("passwordCounts", passwordCounts);
        model.addAttribute("currentUser", user);
        
        return "categories/category-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        CategoryCreationDto categoryDto = new CategoryCreationDto();
        categoryDto.setUserId(user.getId());
        
        model.addAttribute("categoryCreationDto", categoryDto);
        model.addAttribute("category", new Category());
        return "categories/category-form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Category category = categoryService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Verify ownership
            if (!category.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to edit this category");
            }
            
            model.addAttribute("category", categoryMapper.toDto(category));
            return "categories/category-edit";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load category");
            return "redirect:/categories";
        }
    }

    @PostMapping("/{id}")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryCreationDto categoryDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                return "categories/category-edit";
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Category category = categoryService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Verify ownership
            if (!category.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to edit this category");
            }
            
            // Check if name is already taken by another category
            if (!category.getName().equals(categoryDto.getName()) && 
                categoryService.existsByUserIdAndName(user.getId(), categoryDto.getName())) {
                bindingResult.rejectValue("name", "duplicate", "A category with this name already exists");
                return "categories/category-edit";
            }
            
            // Update category
            categoryMapper.updateEntityFromDto(categoryDto, category);
            categoryService.save(category);
            
            redirectAttributes.addFlashAttribute("successMessage", "Category updated successfully");
            return "redirect:/categories";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update category");
            return "redirect:/categories";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Category category = categoryService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            
            // Verify ownership
            if (!category.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to delete this category");
            }
            
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Category deleted successfully");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete category");
        }
        
        return "redirect:/categories";
    }

    @PostMapping
    public String createCategory(
            @Valid @ModelAttribute CategoryCreationDto categoryDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                return "categories/category-form";
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (categoryService.existsByUserIdAndName(user.getId(), categoryDto.getName())) {
                bindingResult.rejectValue("name", "duplicate", "A category with this name already exists");
                return "categories/category-form";
            }
            
            Category category = categoryMapper.toEntity(categoryDto);
            category.setUser(user);
            categoryService.save(category);
            
            redirectAttributes.addFlashAttribute("successMessage", "Category created successfully");
            return "redirect:/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create category");
            return "redirect:/categories";
        }
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