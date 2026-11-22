package com.prahkel.library.controller.exceptionHandler;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prahkel.library.controller.CatalogController;
import com.prahkel.library.service.BookService;

@WebMvcTest(CatalogController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Test
    void handleValidation_shouldReturn400WithFieldErrors() throws Exception {
        String invalidBody = """
                {"author":"Frank Herbert","genre":"Sci-Fi",
                "isbn":"9780441172719","publishedYear":1965}
                """;

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed: {title=Title is required}"))
                .andExpect(jsonPath("$.path").value("/api/v1/catalog"));

        verify(bookService, never()).createBook(any());
    }

    @Test
    void handleSpringBadRequest_shouldReturn400WhenPathVariableTypeIsInvalid() throws Exception {
        mockMvc.perform(get("/api/v1/catalog/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(containsString("abc")))
                .andExpect(jsonPath("$.path").value("/api/v1/catalog/abc"));
    }

    @Test
    void handleInternalServerError_shouldReturn500WhenServiceThrowsUnexpectedException() throws Exception {
        when(bookService.getBookById(1L)).thenThrow(new RuntimeException("simulated database failure"));

        mockMvc.perform(get("/api/v1/catalog/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.path").value("/api/v1/catalog/1"));
    }
}