package dev.portfolio.warehouse.api.domain.inventory

import jakarta.persistence.criteria.JoinType
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InventoryQueryService(
    private val inventoryRepository: InventoryRepository,
) {
    @Transactional(readOnly = true)
    fun search(
        clientCompanyId: Long?,
        warehouseId: Long?,
        status: String?,
        keyword: String?,
    ): List<InventoryResponse> =
        inventoryRepository.findAll(
            inventorySearchSpec(
                clientCompanyId = clientCompanyId,
                warehouseId = warehouseId,
                status = status,
                keyword = keyword,
            ),
            Sort.by(Sort.Direction.ASC, "id"),
        ).map { it.toResponse() }
}

private fun inventorySearchSpec(
    clientCompanyId: Long?,
    warehouseId: Long?,
    status: String?,
    keyword: String?,
): Specification<InventoryEntity> =
    Specification { root, _, criteriaBuilder ->
        root.fetch<Any, Any>("clientCompany", JoinType.LEFT)
        root.fetch<Any, Any>("warehouse", JoinType.LEFT)
        root.fetch<Any, Any>("location", JoinType.LEFT)
        root.fetch<Any, Any>("sku", JoinType.LEFT)

        val normalizedKeyword = keyword?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
        val predicates = listOfNotNull(
            clientCompanyId?.let {
                criteriaBuilder.equal(root.get<Any>("clientCompany").get<Long>("id"), it)
            },
            warehouseId?.let {
                criteriaBuilder.equal(root.get<Any>("warehouse").get<Long>("id"), it)
            },
            status?.takeIf { it != "전체" }?.let {
                when (it) {
                    "부족주의" -> criteriaBuilder.lessThanOrEqualTo(root.get("availableQuantity"), 0)
                    "정상" -> criteriaBuilder.greaterThan(root.get("availableQuantity"), 0)
                    else -> null
                }
            },
            normalizedKeyword?.let {
                criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<Any>("sku").get("code")), "%$it%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<Any>("sku").get("name")), "%$it%"),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<Any>("location").get("code")), "%$it%"),
                )
            },
        )

        criteriaBuilder.and(*predicates.toTypedArray())
    }
