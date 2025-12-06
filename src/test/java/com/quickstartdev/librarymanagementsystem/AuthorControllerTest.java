package com.knf.dev.librarymanagementsystem;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.knf.dev.librarymanagementsystem.service.AuthorService;
import com.knf.dev.librarymanagementsystem.entity.Author;
import com.knf.dev.librarymanagementsystem.controller.AuthorController;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

@WebMvcTest(controllers = AuthorController.class)
class AuthorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthorService authorService;

    @Test
    void testFindAuthorById_Existing() throws Exception {
        Author a = new Author();
        a.setId(1L);
        a.setName("Some Author");
        Mockito.when(authorService.findAuthorById(1L)).thenReturn(Optional.of(a));

        mockMvc.perform(get("/author/1"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("author"))
            .andExpect(view().name("list-author"));
    }

    @Test
    void testFindAllAuthors() throws Exception {

        mockMvc.perform(get("/authors"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("authors"))
            .andExpect(view().name("list-authors"));
    }
}
