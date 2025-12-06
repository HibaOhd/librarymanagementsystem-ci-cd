package com.knf.dev.librarymanagementsystem;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.knf.dev.librarymanagementsystem.service.AuthorService;
import com.knf.dev.librarymanagementsystem.entity.Author;
import com.knf.dev.librarymanagementsystem.repository.AuthorRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;  

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAllAuthors_ReturnsAuthorsList() {
        Author a1 = new Author();
        a1.setId(1L);
        a1.setName("Author One");
        Author a2 = new Author();
        a2.setId(2L);
        a2.setName("Author Two");

        when(authorRepository.findAll()).thenReturn(List.of(a1, a2));

        List<Author> result = authorService.findAllAuthors(); 
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(authorRepository, times(1)).findAll();
    }
    @Test
    void testFindAuthorById_Found() {
        Author a = new Author();
        a.setId(1L);
        a.setName("Some Author");
        when(authorRepository.findById(1L)).thenReturn(Optional.of(a));
    
        Optional<Author> result = authorService.findAuthorById(1L);
        assertTrue(result.isPresent());
        assertEquals("Some Author", result.get().getName());
    }
    
    @Test
    void testFindAuthorById_NotFound() {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());
    
        Optional<Author> result = authorService.findAuthorById(999L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testCreateAuthor() {
        Author a = new Author();
        a.setName("New Author");

        when(authorRepository.save(a)).thenReturn(a);

        Author result = authorService.createAuthor(a);
        assertNotNull(result);
        assertEquals("New Author", result.getName());
        verify(authorRepository, times(1)).save(a);
    }
}
