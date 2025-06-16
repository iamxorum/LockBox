package com.lockbox.controller.web;

import com.lockbox.domain.model.Tag;
import com.lockbox.domain.model.User;
import com.lockbox.domain.service.TagService;
import com.lockbox.domain.service.UserService;
import com.lockbox.dto.TagCreationDto;
import com.lockbox.mapper.TagMapper;
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
@RequestMapping("/tags")
public class TagViewController {

    private final TagService tagService;
    private final UserService userService;
    private final TagMapper tagMapper;

    @Autowired
    public TagViewController(TagService tagService, UserService userService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.userService = userService;
        this.tagMapper = tagMapper;
    }

    @GetMapping
    public String listTags(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Tag> tags = tagService.findByUserId(user.getId());
        Map<Long, Long> passwordCounts = tagService.getPasswordCountsByTag(user.getId());
        
        model.addAttribute("tags", tags);
        model.addAttribute("passwordCounts", passwordCounts);
        model.addAttribute("currentUser", user);
        
        return "tags/tag-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        TagCreationDto tagDto = new TagCreationDto();
        tagDto.setUserId(user.getId());
        
        model.addAttribute("tagCreationDto", tagDto);
        model.addAttribute("currentUser", user);
        return "tags/tag-form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Tag tag = tagService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tag not found"));
            
            // Verify ownership
            if (!tag.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to edit this tag");
            }
            
            TagCreationDto tagDto = new TagCreationDto();
            tagDto.setId(tag.getId());
            tagDto.setName(tag.getName());
            tagDto.setDescription(tag.getDescription());
            tagDto.setColor(tag.getColor());
            tagDto.setUserId(tag.getUser().getId());
            
            model.addAttribute("tag", tagDto);
            return "tags/tag-edit";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tags";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to load tag");
            return "redirect:/tags";
        }
    }

    @PostMapping("/{id}")
    public String updateTag(
            @PathVariable Long id,
            @Valid @ModelAttribute("tag") TagCreationDto tagDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                return "tags/tag-edit";
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Tag tag = tagService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tag not found"));
            
            // Verify ownership
            if (!tag.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to edit this tag");
            }
            
            // Check if name is already taken by another tag
            if (!tag.getName().equals(tagDto.getName()) && 
                tagService.existsByNameAndUserId(tagDto.getName(), user.getId())) {
                bindingResult.rejectValue("name", "duplicate", "A tag with this name already exists");
                return "tags/tag-edit";
            }
            
            // Update tag
            tagMapper.updateEntityFromDto(tagDto, tag);
            tagService.save(tag);
            
            redirectAttributes.addFlashAttribute("successMessage", "Tag updated successfully");
            return "redirect:/tags";
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/tags";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update tag");
            return "redirect:/tags";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteTag(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Tag tag = tagService.findById(id)
                    .orElseThrow(() -> new RuntimeException("Tag not found"));
            
            // Verify ownership
            if (!tag.getUser().getId().equals(user.getId())) {
                throw new SecurityException("You don't have permission to delete this tag");
            }
            
            tagService.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Tag deleted successfully");
        } catch (SecurityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete tag");
        }
        
        return "redirect:/tags";
    }

    @PostMapping
    public String createTag(
            @Valid @ModelAttribute TagCreationDto tagDto,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                return "tags/tag-form";
            }

            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (tagService.existsByNameAndUserId(tagDto.getName(), user.getId())) {
                bindingResult.rejectValue("name", "duplicate", "A tag with this name already exists");
                return "tags/tag-form";
            }
            
            Tag tag = tagMapper.toEntity(tagDto);
            tag.setUser(user);
            tagService.save(tag);
            
            redirectAttributes.addFlashAttribute("successMessage", "Tag created successfully");
            return "redirect:/tags";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create tag");
            return "redirect:/tags";
        }
    }

    /**
     * Alternative endpoint that accepts JSON for AJAX requests
     */
    @PostMapping(value = "/api", consumes = "application/json")
    @ResponseBody
    public ResponseEntity<?> createTagJson(@Valid @RequestBody TagCreationDto tagCreationDto, 
                                             HttpServletRequest request) {
        try {
            // Creating tag via JSON API
            User user = userService.findById(tagCreationDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (tagService.existsByNameAndUserId(tagCreationDto.getName(), user.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A tag with this name already exists");
            }

            Tag tag = tagMapper.toEntity(tagCreationDto);
            tag.setUser(user);
            Tag savedTag = tagService.save(tag);
            
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(tagMapper.toDto(savedTag));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Failed to create tag: " + e.getMessage());
        }
    }
} 