package dev.portfolio.warehouse.api.domain.client

import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/client-companies")
class ClientCompanyController(
    private val clientCompanyRepository: ClientCompanyRepository,
) {
    @GetMapping
    fun search(): List<ClientCompanyResponse> =
        clientCompanyRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
            .map { it.toResponse() }
}
