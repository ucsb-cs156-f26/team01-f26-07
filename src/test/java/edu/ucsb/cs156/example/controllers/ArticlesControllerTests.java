package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticlesRepository articlesRepository;
  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/Articles").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    LocalDateTime ldt = LocalDateTime.parse("2026-10-21T16:50:00");

    Articles article =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("testing an article creation")
            .email("adi@google.com")
            .title("1st article")
            .dateAdded(ldt)
            .build();

    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article));
    MvcResult response =
        mockMvc
            .perform(get("/api/Articles").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T16:50:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2026-10-22T12:00:00");

    Articles articleOrig =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("first article test")
            .title("1st article")
            .email("adi@google.com")
            .dateAdded(ldt1)
            .build();

    Articles articleEdited =
        Articles.builder()
            .url("https://example.com/article2")
            .explanation("updated article test")
            .title("updated article")
            .email("test@gmail.com")
            .dateAdded(ldt2)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(articleOrig));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(67L);
    verify(articlesRepository, times(1)).save(articleEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_article_that_does_not_exist() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T16:50:00");

    Articles articleEdited =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("testing an article creation")
            .title("1st article")
            .email("adi@google.com")
            .dateAdded(ldt1)
            .build();

    String requestBody = mapper.writeValueAsString(articleEdited);

    when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/Articles")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 67 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {
    when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());
    MvcResult response =
        mockMvc
            .perform(get("/api/Articles").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("Articles with id 7 not found", json.get("message"));
  }

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/Articles/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/Articles/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/Articles/post")
                .param("url", "https://example.com/article1")
                .param("explanation", "testing an article creation")
                .param("title", "1st article")
                .param("email", "adi@google.com")
                .param("dateAdded", "2026-10-21T16:50:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/Articles/post")
                .param("url", "https://example.com/article1")
                .param("explanation", "testing an article creation")
                .param("title", "1st article")
                .param("email", "adi@google.com")
                .param("dateAdded", "2026-10-21T16:50:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    LocalDateTime date1 = LocalDateTime.parse("2026-10-21T16:50:00");
    LocalDateTime date2 = LocalDateTime.parse("2026-10-22T12:00:00");

    Articles article1 =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("first article test")
            .title("1st article")
            .email("adi@google.com")
            .dateAdded(date1)
            .build();

    Articles article2 =
        Articles.builder()
            .url("https://example.com/article2")
            .explanation("another article test")
            .title("2nd article")
            .email("test@gmail.com")
            .dateAdded(date2)
            .build();

    List<Articles> expectedArticles = Arrays.asList(article1, article2);

    when(articlesRepository.findAll()).thenReturn(expectedArticles);

    MvcResult response =
        mockMvc.perform(get("/api/Articles/all")).andExpect(status().isOk()).andReturn();

    verify(articlesRepository, times(1)).findAll();

    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {

    LocalDateTime dateAdded = LocalDateTime.parse("2026-10-21T16:50:00");

    Articles article =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("testing an article creation")
            .title("1st article")
            .email("adi@google.com")
            .dateAdded(dateAdded)
            .build();

    when(articlesRepository.save(eq(article))).thenReturn(article);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/Articles/post")
                    .param("url", "https://example.com/article1")
                    .param("explanation", "testing an article creation")
                    .param("title", "1st article")
                    .param("email", "adi@google.com")
                    .param("dateAdded", "2026-10-21T16:50:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).save(article);

    String expectedJson = mapper.writeValueAsString(article);
    String responseString = response.getResponse().getContentAsString();

    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_article() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2026-10-21T16:50:00");

    Articles article =
        Articles.builder()
            .url("https://example.com/article1")
            .explanation("first article test")
            .title("1st article")
            .email("adi@google.com")
            .dateAdded(ldt1)
            .build();

    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.of(article));

    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(articlesRepository, times(1)).findById(15L);
    verify(articlesRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_article_and_gets_right_error_message()
      throws Exception {
    when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/Articles").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(articlesRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("Articles with id 15 not found", json.get("message"));
  }
}
