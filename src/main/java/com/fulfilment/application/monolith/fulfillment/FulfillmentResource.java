package com.fulfilment.application.monolith.fulfillment;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;

/**
 * REST API endpoints for managing warehouse-product-store fulfillment units
 * Handles creation, removal, and retrieval of fulfillment associations
 */
@Path("fulfillment")
@RequestScoped
@Produces("application/json")
@Consumes("application/json")
public class FulfillmentResource {

  @Inject WarehouseProductStoreRepository fulfillmentRepository;
  @Inject CreateFulfillmentUnitUseCase createFulfillmentUnitUseCase;

  private static final Logger LOGGER = Logger.getLogger(FulfillmentResource.class.getName());

  /**
   * Create a new fulfillment unit (warehouse-product-store association)
   * POST /fulfillment/warehouse/{warehouseId}/product/{productId}/store/{storeId}
   */
  @POST
  @Path("warehouse/{warehouseId}/product/{productId}/store/{storeId}")
  @Transactional
  public Response createFulfillmentUnit(
      @PathParam("warehouseId") Long warehouseId,
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId,
      FulfillmentRequest request) {

    try {
      Integer quantity = (request != null && request.quantityAvailable != null)
          ? request.quantityAvailable : 0;

      createFulfillmentUnitUseCase.createFulfillmentUnit(warehouseId, productId, storeId, quantity);

      LOGGER.info("Fulfillment unit created: W=" + warehouseId + ", P=" + productId + ", S=" + storeId);
      return Response.status(201)
          .entity(new FulfillmentResponse("Fulfillment unit created successfully", 201))
          .build();
    } catch (WebApplicationException e) {
      LOGGER.warn("Failed to create fulfillment unit: " + e.getMessage());
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error creating fulfillment unit", e);
      throw new WebApplicationException("Error creating fulfillment unit: " + e.getMessage(), 500);
    }
  }

  /**
   * Get all fulfillment units for a warehouse
   * GET /fulfillment/warehouse/{warehouseId}
   */
  @GET
  @Path("warehouse/{warehouseId}")
  public List<FulfillmentDTO> getWarehouseFulfillments(@PathParam("warehouseId") Long warehouseId) {
    return fulfillmentRepository.find("warehouseId", warehouseId)
        .list()
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get all warehouses fulfilling a specific store
   * GET /fulfillment/store/{storeId}
   */
  @GET
  @Path("store/{storeId}")
  public List<FulfillmentDTO> getStoreFulfillments(@PathParam("storeId") Long storeId) {
    return fulfillmentRepository.find("storeId", storeId)
        .list()
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get all warehouses fulfilling a specific product in a store
   * GET /fulfillment/product/{productId}/store/{storeId}
   */
  @GET
  @Path("product/{productId}/store/{storeId}")
  public List<FulfillmentDTO> getProductFulfillments(
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId) {

    return fulfillmentRepository.getWarehousesByProductAndStore(productId, storeId)
        .stream()
        .map(this::toDTO)
        .collect(Collectors.toList());
  }

  /**
   * Get specific fulfillment unit details
   * GET /fulfillment/warehouse/{warehouseId}/product/{productId}/store/{storeId}
   */
  @GET
  @Path("warehouse/{warehouseId}/product/{productId}/store/{storeId}")
  public FulfillmentDTO getFulfillmentUnit(
      @PathParam("warehouseId") Long warehouseId,
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId) {

    WarehouseProductStore mapping = fulfillmentRepository
        .getMapping(warehouseId, productId, storeId);

    if (mapping == null) {
      throw new WebApplicationException("Fulfillment unit not found", 404);
    }

    return toDTO(mapping);
  }

  /**
   * Update quantity available for a fulfillment unit
   * PUT /fulfillment/warehouse/{warehouseId}/product/{productId}/store/{storeId}
   */
  @PUT
  @Path("warehouse/{warehouseId}/product/{productId}/store/{storeId}")
  @Transactional
  public FulfillmentDTO updateFulfillmentQuantity(
      @PathParam("warehouseId") Long warehouseId,
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId,
      FulfillmentRequest request) {

    if (request == null || request.quantityAvailable == null) {
      throw new WebApplicationException("Quantity is required", 400);
    }

    createFulfillmentUnitUseCase.updateQuantityAvailable(warehouseId, productId, storeId,
        request.quantityAvailable);

    WarehouseProductStore updated = fulfillmentRepository
        .getMapping(warehouseId, productId, storeId);

    return toDTO(updated);
  }

  /**
   * Remove a fulfillment unit
   * DELETE /fulfillment/warehouse/{warehouseId}/product/{productId}/store/{storeId}
   */
  @DELETE
  @Path("warehouse/{warehouseId}/product/{productId}/store/{storeId}")
  @Transactional
  public Response removeFulfillmentUnit(
      @PathParam("warehouseId") Long warehouseId,
      @PathParam("productId") Long productId,
      @PathParam("storeId") Long storeId) {

    try {
      createFulfillmentUnitUseCase.removeFulfillmentUnit(warehouseId, productId, storeId);
      LOGGER.info("Fulfillment unit removed: W=" + warehouseId + ", P=" + productId + ", S=" + storeId);
      return Response.status(204).build();
    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error("Error removing fulfillment unit", e);
      throw new WebApplicationException("Error removing fulfillment unit: " + e.getMessage(), 500);
    }
  }

  /**
   * Get fulfillment constraints info
   * GET /fulfillment/constraints
   */
  @GET
  @Path("constraints")
  public ConstraintsInfo getConstraints() {
    return new ConstraintsInfo(
        "Each Product can be fulfilled by max 2 warehouses per Store",
        "Each Store can be fulfilled by max 3 warehouses",
        "Each Warehouse can store max 5 types of Products"
    );
  }

  private FulfillmentDTO toDTO(WarehouseProductStore mapping) {
    FulfillmentDTO dto = new FulfillmentDTO();
    dto.id = mapping.id;
    dto.warehouseId = mapping.warehouseId;
    dto.productId = mapping.productId;
    dto.storeId = mapping.storeId;
    dto.quantityAvailable = mapping.quantityAvailable;
    dto.createdAt = mapping.createdAt;
    return dto;
  }

  public static class FulfillmentRequest {
    public Integer quantityAvailable;
  }

  public static class FulfillmentDTO {
    public Long id;
    public Long warehouseId;
    public Long productId;
    public Long storeId;
    public Integer quantityAvailable;
    public java.time.LocalDateTime createdAt;
  }

  public static class FulfillmentResponse {
    public String message;
    public int statusCode;

    public FulfillmentResponse(String message, int statusCode) {
      this.message = message;
      this.statusCode = statusCode;
    }
  }

  public static class ConstraintsInfo {
    public String constraint1;
    public String constraint2;
    public String constraint3;

    public ConstraintsInfo(String constraint1, String constraint2, String constraint3) {
      this.constraint1 = constraint1;
      this.constraint2 = constraint2;
      this.constraint3 = constraint3;
    }
  }
}

