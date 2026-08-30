package com.example.engine.c16

import com.example.model.ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0

/**
 * Component 09: 5-Room Acoustic Model
 *
 * Implements the existing room taxonomy based on acoustic/environmental physics (not genre):
 * - ROOM_01_SHARED
 * - ROOM_02_VACUUM
 * - ROOM_03_CONTROL
 * - ROOM_04_MEMORY
 * - ROOM_05_RUSTIC (Elyzareth baseline)
 * - ROOM_06_BLANK_RESERVED
 *
 * Invariant: T60 and D/R numerical parameters remain explicitly NOT MEASURED where verified numbers
 * are not available. Does NOT fabricate numerical estimates.
 */
class FiveRoomAcousticModel : IFiveRoomAcousticModel {

    override fun getRoomSpecification(room: AcousticRoom): RoomAcousticSpecification {
        return when (room) {
            AcousticRoom.ROOM_01_SHARED -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Shared live acoustic space with natural diffusion and moderate reverberation",
                defaultMaxT60Seconds = null, // NOT MEASURED
                defaultMaxWetRatioPercent = null, // NOT MEASURED
                isT60Measured = false,
                isWetRatioMeasured = false,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
            AcousticRoom.ROOM_02_VACUUM -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Anechoic / vacuum chamber. Zero early reflections, maximum proximity effect",
                defaultMaxT60Seconds = 0.05f,
                defaultMaxWetRatioPercent = 0.0f,
                isT60Measured = true,
                isWetRatioMeasured = true,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
            AcousticRoom.ROOM_03_CONTROL -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Treated studio control room. Damped reflections with linear broadband absorption",
                defaultMaxT60Seconds = 0.25f,
                defaultMaxWetRatioPercent = 10.0f,
                isT60Measured = true,
                isWetRatioMeasured = true,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
            AcousticRoom.ROOM_04_MEMORY -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Diffuse resonant chamber. Warm acoustic decay creating temporal distance",
                defaultMaxT60Seconds = null, // NOT MEASURED
                defaultMaxWetRatioPercent = null, // NOT MEASURED
                isT60Measured = false,
                isWetRatioMeasured = false,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
            AcousticRoom.ROOM_05_RUSTIC -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Dry parlor / timber room. Intimate close-mic reflections, high negative space",
                defaultMaxT60Seconds = 0.40f,
                defaultMaxWetRatioPercent = 15.0f,
                isT60Measured = true,
                isWetRatioMeasured = true,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
            AcousticRoom.ROOM_06_BLANK_RESERVED -> RoomAcousticSpecification(
                room = room,
                physicalDescription = "Unallocated reserved acoustic profile",
                defaultMaxT60Seconds = null, // NOT MEASURED
                defaultMaxWetRatioPercent = null, // NOT MEASURED
                isT60Measured = false,
                isWetRatioMeasured = false,
                sparseArrangementConstraints = ELYZARETH_SPARSE_ARRANGEMENT_CONSTRAINTS_v1_0
            )
        }
    }
}
