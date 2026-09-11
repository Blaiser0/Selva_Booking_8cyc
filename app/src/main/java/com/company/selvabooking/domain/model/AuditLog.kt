package com.company.selvabooking.domain.model

enum class AuditAction(val label: String) {
    CREATE("Creación"),
    UPDATE("Modificación"),
    DELETE("Eliminación")
}

enum class AuditEntityType(val label: String) {
    HOTEL("Hotel"),
    ROOM("Habitación")
}

data class AuditLog(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val actorUserId: String = "",
    val actorName: String = "",
    val actorEmail: String = "",
    val action: AuditAction = AuditAction.CREATE,
    val entityType: AuditEntityType = AuditEntityType.HOTEL,
    val entityId: String = "",
    val entityLabel: String = "",
    val hotelId: String = "",
    val beforeData: Map<String, Any?> = emptyMap(),
    val afterData: Map<String, Any?> = emptyMap(),
    val relatedData: List<Map<String, Any?>> = emptyList(),
    val reverted: Boolean = false,
    val revertedAt: Long = 0L,
    val revertedBy: String = ""
) {
    val canRevert: Boolean get() = !reverted

    fun toMap(): Map<String, Any?> = buildMap {
        put("timestamp", timestamp)
        put("actorUserId", actorUserId)
        put("actorName", actorName)
        put("actorEmail", actorEmail)
        put("action", action.name)
        put("entityType", entityType.name)
        put("entityId", entityId)
        put("entityLabel", entityLabel)
        put("hotelId", hotelId)
        if (beforeData.isNotEmpty()) put("beforeData", beforeData)
        if (afterData.isNotEmpty()) put("afterData", afterData)
        if (relatedData.isNotEmpty()) put("relatedData", relatedData)
        put("reverted", reverted)
        if (revertedAt > 0L) put("revertedAt", revertedAt)
        if (revertedBy.isNotBlank()) put("revertedBy", revertedBy)
    }

    companion object {
        private const val ENTITY_ID_KEY = "_entityId"

        @Suppress("UNCHECKED_CAST")
        fun fromMap(id: String, map: Map<String, Any?>): AuditLog = AuditLog(
            id = id,
            timestamp = (map["timestamp"] as? Number)?.toLong() ?: 0L,
            actorUserId = map["actorUserId"] as? String ?: "",
            actorName = map["actorName"] as? String ?: "",
            actorEmail = map["actorEmail"] as? String ?: "",
            action = map["action"]?.toString()?.let { name ->
                AuditAction.entries.find { it.name == name }
            } ?: AuditAction.CREATE,
            entityType = map["entityType"]?.toString()?.let { name ->
                AuditEntityType.entries.find { it.name == name }
            } ?: AuditEntityType.HOTEL,
            entityId = map["entityId"] as? String ?: "",
            entityLabel = map["entityLabel"] as? String ?: "",
            hotelId = map["hotelId"] as? String ?: "",
            beforeData = (map["beforeData"] as? Map<String, Any?>) ?: emptyMap(),
            afterData = (map["afterData"] as? Map<String, Any?>) ?: emptyMap(),
            relatedData = (map["relatedData"] as? List<Map<String, Any?>>) ?: emptyList(),
            reverted = map["reverted"] as? Boolean ?: false,
            revertedAt = (map["revertedAt"] as? Number)?.toLong() ?: 0L,
            revertedBy = map["revertedBy"] as? String ?: ""
        )

        fun hotelSnapshot(hotel: Hotel): Map<String, Any?> =
            buildMap {
                put(ENTITY_ID_KEY, hotel.id)
                putAll(hotel.toMap())
            }

        fun roomSnapshot(room: Room): Map<String, Any?> =
            buildMap {
                put(ENTITY_ID_KEY, room.id)
                putAll(room.toMap())
            }

        fun hotelFromSnapshot(map: Map<String, Any?>): Hotel {
            val id = map[ENTITY_ID_KEY] as? String ?: ""
            val data = map.filterKeys { it != ENTITY_ID_KEY }
            return Hotel.fromMap(id, data)
        }

        fun roomFromSnapshot(map: Map<String, Any?>): Room {
            val id = map[ENTITY_ID_KEY] as? String ?: ""
            val data = map.filterKeys { it != ENTITY_ID_KEY }
            return Room.fromMap(id, data)
        }
    }
}
