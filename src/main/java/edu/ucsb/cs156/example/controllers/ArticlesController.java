package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.Articles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for Articles */
@Tag(name = "Articles")
@RequestMapping("/api/Articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {
  @Operation(summary = "Update an existing article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public Articles articleDate(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid Articles incoming) {

    Articles article =
        articleRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    article.setExplanation(incoming.getExplanation());
    article.setTitle(incoming.getTitle());
    article.setDateAdded(incoming.getDateAdded());
    article.setUrl(incoming.getUrl());
    article.setEmail(incoming.getEmail());

    articleRepository.save(article);

    return article;
  }

  @Autowired ArticlesRepository articleRepository;

  @Operation(summary = "Delete an Article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteArticle(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        articleRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    articleRepository.delete(article);
    return genericMessage("Articles with id %s deleted".formatted(id));
  }

  /**
   * List all articles
   *
   * @return an iterable of Articles
   */
  @Operation(summary = "List all articles")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<Articles> allArticles() {
    Iterable<Articles> articles = articleRepository.findAll();
    return articles;
  }

  @Operation(summary = "Get a single article")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public Articles getById(@Parameter(name = "id") @RequestParam Long id) {
    Articles article =
        articleRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(Articles.class, id));

    return article;
  }

  @Operation(summary = "Create a new article")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public Articles postArticle(
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(name = "url") @RequestParam String url,
      @Parameter(name = "title") @RequestParam String title,
      @Parameter(name = "email") @RequestParam String email,
      @Parameter(
              name = "dateAdded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateAdded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateAdded)
      throws JsonProcessingException {

    log.info("dateAdded={}", dateAdded);

    Articles article = new Articles();
    article.setUrl(url);
    article.setExplanation(explanation);
    article.setTitle(title);
    article.setEmail(email);
    article.setDateAdded(dateAdded);

    Articles savedArticle = articleRepository.save(article);

    return savedArticle;
  }
}
