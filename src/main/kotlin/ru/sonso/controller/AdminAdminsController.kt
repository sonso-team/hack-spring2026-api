package ru.sonso.controller

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.sonso.dto.AdminFull
import ru.sonso.dto.SuccessResponse
import ru.sonso.dto.request.CreateAdminRequest
import ru.sonso.service.AdminAdminsService
import java.util.UUID

@RestController
@RequestMapping("/api/admin/admins")
class AdminAdminsController(
    private val adminAdminsService: AdminAdminsService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun getAdmins(): ResponseEntity<List<AdminFull>> {
        logger.info("GET /api/admin/admins requested")
        val admins = adminAdminsService.getAllAdmins()
        logger.info("GET /api/admin/admins completed, count={}", admins.size)
        return ResponseEntity.ok(admins)
    }

    @PostMapping
    fun createAdmin(@RequestBody request: CreateAdminRequest): ResponseEntity<AdminFull> {
        logger.info("POST /api/admin/admins requested, email={}", request.email)
        val created = adminAdminsService.createAdmin(request)
        logger.info("POST /api/admin/admins completed, adminId={}", created.id)
        return ResponseEntity.ok(created)
    }

    @DeleteMapping("/{id}")
    fun deleteAdmin(@PathVariable id: UUID): ResponseEntity<SuccessResponse> {
        logger.info("DELETE /api/admin/admins/{} requested", id)
        val result = adminAdminsService.deleteAdmin(id)
        logger.info("DELETE /api/admin/admins/{} completed, success={}", id, result.success)
        return ResponseEntity.ok(result)
    }
}
