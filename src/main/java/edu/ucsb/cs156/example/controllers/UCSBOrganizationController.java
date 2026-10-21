package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBOrganization */
@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  /**
   * THis method returns a list of all UCSBOrganization.
   *
   * @return a list of all UCSBOrganizations
   */
  @Operation(summary = "List all ucsb organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allorgss() {
    Iterable<UCSBOrganization> orgs = ucsbOrganizationRepository.findAll();
    return orgs;
  }

  /**
   * This method creates a new org. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode orgCode of the organization (e.g. ZPR)
   * @param orgTranslationShort short name of the organization
   * @param orgTranslation full name of the organization
   * @param inactive whether or not the organization is inactive
   * @return the saved organization
   */
  @Operation(summary = "Create a new organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode", description = "Organization orgCode, e.g. ZPR") @RequestParam
          String orgCode,
      @Parameter(name = "orgTranslationShort", description = "Short name, e.g. ZETA PHI RHO")
          @RequestParam
          String orgTranslationShort,
      @Parameter(name = "orgTranslation", description = "Full name, e.g. ZETA PHI RHO")
          @RequestParam
          String orgTranslation,
      @Parameter(name = "inactive", description = "true if the organization is inactive")
          @RequestParam
          boolean inactive) {

    UCSBOrganization org = new UCSBOrganization();
    org.setOrgCode(orgCode);
    org.setOrgTranslationShort(orgTranslationShort);
    org.setOrgTranslation(orgTranslation);
    org.setInactive(inactive);

    UCSBOrganization savedOrg = ucsbOrganizationRepository.save(org);
    return savedOrg;
  }

  /**
   * PUTTTTT Update a single UCSB organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode orgCode of the organization to update
   * @param incoming the new values for the organization
   * @return the updated organization
   */
  @Operation(summary = "Update a single UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(name = "orgCode", description = "Code of the organization to update, e.g. ZPR")
          @RequestParam
          String orgCode,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    org.setOrgTranslationShort(incoming.getOrgTranslationShort());
    org.setOrgTranslation(incoming.getOrgTranslation());
    org.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(org);

    return org;
  }

  /**
   * This method returns a single UCSB organization. GET
   *
   * @param orgCode orgCode of the organization
   * @return a single UCSB organization
   */
  @Operation(summary = "Get a single UCSB organization")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    return org;
  }

  // DELETE
  /**
   * Delete an organization. Accessible only to users with the role "ROLE_ADMIN".
   *
   * @param orgCode code of the organization to delete
   * @return a message indiciating the organization was deleted
   */
  @Operation(summary = "Delete a UCSBOrganization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteOrganization(@Parameter(name = "orgCode") @RequestParam String orgCode) {
    UCSBOrganization org =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    ucsbOrganizationRepository.delete(org);
    return genericMessage("UCSBOrganization with id %s deleted".formatted(orgCode));
  }
}
