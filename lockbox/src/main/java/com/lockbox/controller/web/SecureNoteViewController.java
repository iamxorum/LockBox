package com.lockbox.controller.web;

import com.lockbox.dto.SecureNoteCreationDto;
import com.lockbox.dto.SecureNoteDto;
import com.lockbox.domain.model.Category;
import com.lockbox.domain.model.SecureNote;
import com.lockbox.domain.model.User;
import com.lockbox.domain.model.Tag;
import com.lockbox.domain.service.AuditLogService;
import com.lockbox.domain.service.CategoryService;
import com.lockbox.domain.service.SecureNoteService;
import com.lockbox.domain.service.UserService;
import com.lockbox.domain.service.TagService;
import com.lockbox.mapper.SecureNoteMapper;
import com.lockbox.mapper.TagMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/secure-notes")
public class SecureNoteViewController {
    private static final Logger logger = LoggerFactory.getLogger(SecureNoteViewController.class);
    
    private final SecureNoteService secureNoteService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final AuditLogService auditLogService;
    private final SecureNoteMapper secureNoteMapper;
    private final TagService tagService;
    private final TagMapper tagMapper;

    public SecureNoteViewController(SecureNoteService secureNoteService,
                                  CategoryService categoryService,
                                  UserService userService,
                                  AuditLogService auditLogService,
                                  SecureNoteMapper secureNoteMapper,
                                  TagService tagService,
                                  TagMapper tagMapper) {
        this.secureNoteService = secureNoteService;
        this.categoryService = categoryService;
        this.userService = userService;
        this.auditLogService = auditLogService;
        this.secureNoteMapper = secureNoteMapper;
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new SecurityException("User not found"));
        List<Category> categories = categoryService.findByUserId(user.getId());
        
        SecureNoteCreationDto note = new SecureNoteCreationDto();
        note.setUserId(user.getId());
        
        model.addAttribute("secureNote", note);
        model.addAttribute("categories", categories);
        model.addAttribute("currentUserId", user.getId());
        return "secure-notes/secure-note-form";
    }

    @PostMapping("/save")
    public String saveNote(@Valid @ModelAttribute("secureNote") SecureNoteCreationDto noteDto,
                         BindingResult result,
                         Model model,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            logger.error("Validation errors: {}", result.getAllErrors());
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            List<Category> categories = categoryService.findByUserId(user.getId());
            model.addAttribute("categories", categories);
            model.addAttribute("currentUserId", user.getId());
            return "secure-notes/secure-note-form";
        }

        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            noteDto.setUserId(user.getId());
            
            // Convert DTO to entity
            SecureNote secureNote;
            if (noteDto.getId() != null) {
                // Update existing note
                secureNote = secureNoteService.findByIdWithTags(noteDto.getId())
                        .orElseThrow(() -> new IllegalArgumentException("Note not found"));
                if (!secureNote.getUser().getId().equals(user.getId())) {
                    throw new SecurityException("Access denied");
                }
                secureNoteMapper.updateEntityFromDto(noteDto, secureNote);
            } else {
                // Create new note
                secureNote = secureNoteMapper.toEntity(noteDto);
                secureNote.setUser(user);
            }
            
            // Set category if provided
            if (noteDto.getCategoryId() != null) {
                Category category = categoryService.findById(noteDto.getCategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Category not found"));
                secureNoteMapper.setCategoryInSecureNote(secureNote, category);
            }
            
            // Handle tags
            logger.info("Processing tag IDs: {}", noteDto.getTagIds());
            // Clear existing tags
            secureNote.getTags().clear();
            if (noteDto.getTagIds() != null && !noteDto.getTagIds().isEmpty()) {
                for (Long tagId : noteDto.getTagIds()) {
                    Tag tag = tagService.findById(tagId)
                            .orElseThrow(() -> new IllegalArgumentException("Tag not found: " + tagId));
                    // Verify the tag belongs to the current user
                    if (!tag.getUser().getId().equals(user.getId())) {
                        throw new SecurityException("Access denied to tag: " + tagId);
                    }
                    logger.info("Adding tag {} to note", tag.getName());
                    secureNote.getTags().add(tag);
                }
            }
            
            // Save and convert back to DTO
            logger.info("Saving note with {} tags", secureNote.getTags().size());
            SecureNote savedNote = secureNoteService.save(secureNote);
            logger.info("Saved note. Tags after save: {}", savedNote.getTags().size());
            SecureNoteDto savedNoteDto = secureNoteMapper.toDto(savedNote);
            
            auditLogService.createAuditLog(user.getId(), "CREATE_NOTE", "SecureNote", savedNote.getId(), "Note created");
            
            redirectAttributes.addFlashAttribute("successMessage", "Secure note saved successfully");
            return "redirect:/secure-notes";
        } catch (Exception e) {
            logger.error("Error saving note", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving secure note: " + e.getMessage());
            return "redirect:/secure-notes/new";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            SecureNote note = secureNoteService.findByIdWithTags(id)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));
            
            if (!note.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Access denied");
            }
            
            List<Category> categories = categoryService.findByUserId(user.getId());
            SecureNoteDto noteDto = secureNoteMapper.toDto(note);
            
            model.addAttribute("secureNote", noteDto);
            model.addAttribute("categories", categories);
            model.addAttribute("currentUserId", user.getId());
            return "secure-notes/secure-note-form";
        } catch (Exception e) {
            logger.error("Error showing edit form", e);
            return "redirect:/secure-notes?error=" + e.getMessage();
        }
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String listNotes(Model model, Authentication authentication,
                          @RequestParam(required = false) Long categoryId) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            List<SecureNote> notes;
            
            if (categoryId != null) {
                notes = secureNoteService.findByUserId(user.getId()).stream()
                        .filter(note -> note.getCategory() != null && note.getCategory().getId().equals(categoryId))
                        .collect(Collectors.toList());
            } else {
                notes = secureNoteService.findByUserId(user.getId());
            }
            
            logger.info("Found {} notes for user {}", notes.size(), user.getId());
            
            // Initialize tags for each note to prevent lazy loading issues
            notes.forEach(note -> {
                if (note.getTags() != null) {
                    note.getTags().size(); // Force initialization of tags
                }
            });
            
            List<Category> categories = categoryService.findByUserId(user.getId());
            List<SecureNoteDto> noteDtos = notes.stream()
                .map(note -> {
                    SecureNoteDto dto = secureNoteMapper.toDto(note);
                    if (note.getCategory() != null) {
                        // Initialize the category to prevent lazy loading issues
                        Category category = note.getCategory();
                        dto.setCategoryName(category.getName());
                    }
                    // Map tags
                    dto.setTags(note.getTags().stream()
                            .map(tagMapper::toDto)
                            .collect(Collectors.toSet()));
                    return dto;
                })
                .collect(Collectors.toList());
            
            logger.info("Converted to {} DTOs", noteDtos.size());
            
            model.addAttribute("secureNotes", noteDtos);
            model.addAttribute("categories", categories);
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("currentUserId", user.getId());
            return "secure-notes/secure-note-list";
        } catch (Exception e) {
            logger.error("Error listing notes", e);
            model.addAttribute("errorMessage", "Error loading secure notes: " + e.getMessage());
            model.addAttribute("secureNotes", List.of());
            return "secure-notes/secure-note-list";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteNote(@PathVariable Long id, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            SecureNote note = secureNoteService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));
            
            if (!note.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Access denied");
            }
            
            secureNoteService.delete(note);
            auditLogService.createAuditLog(user.getId(), "DELETE", "SecureNote", id, "Note deleted");
            
            redirectAttributes.addFlashAttribute("successMessage", "Secure note deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting secure note: " + e.getMessage());
        }
        return "redirect:/secure-notes";
    }

    @GetMapping("/view/{id}")
    public String viewNote(@PathVariable Long id, Model model, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new SecurityException("User not found"));
            
            // Get the note by ID
            SecureNote note = secureNoteService.findByIdWithTags(id)
                    .orElseThrow(() -> new IllegalArgumentException("Note not found"));
            
            if (!note.getUser().getId().equals(user.getId())) {
                throw new SecurityException("Access denied");
            }
            
            // Force initialization of category to prevent lazy loading issues
            if (note.getCategory() != null) {
                Category category = categoryService.findById(note.getCategory().getId())
                        .orElse(null);
                if (category != null) {
                    // Set the initialized category back to the note
                    note.setCategory(category);
                }
            }
            
            SecureNoteDto noteDto = secureNoteMapper.toDto(note);
            
            // Add category name if available
            if (note.getCategory() != null) {
                noteDto.setCategoryName(note.getCategory().getName());
            }
            
            // Map tags
            noteDto.setTags(note.getTags().stream()
                    .map(tagMapper::toDto)
                    .collect(Collectors.toSet()));
            
            model.addAttribute("secureNote", noteDto);
            model.addAttribute("currentUserId", user.getId());
            
            // Log the view action
            auditLogService.createAuditLog(user.getId(), "VIEW", "SecureNote", id, "Note viewed");
            
            return "secure-notes/secure-note-view";
        } catch (Exception e) {
            logger.error("Error viewing note", e);
            redirectAttributes.addFlashAttribute("error", "Error viewing secure note: " + e.getMessage());
            return "redirect:/secure-notes";
        }
    }

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/secure-notes";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/secure-notes";
    }
} 