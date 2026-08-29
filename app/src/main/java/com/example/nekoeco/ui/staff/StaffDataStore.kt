package com.example.nekoeco.ui.staff

import androidx.compose.runtime.mutableStateListOf

import com.example.nekoeco.ui.customer.CustomerAppointment
import com.example.nekoeco.ui.customer.CustomerDataStore
import com.example.nekoeco.ui.customer.CustomerDevice
import com.example.nekoeco.ui.customer.CustomerPayment


// ============================================================
// COMPONENT DATA
// ============================================================

data class StaffComponent(
    val name: String,
    var condition: String,
    var price: String,
    var status: String
)


// ============================================================
// DEVICE DATA
// ============================================================

data class StaffDevice(
    val id: String,
    val deviceName: String,
    val category: String,
    val customer: String,
    val date: String,
    var status: String,
    val recoveryValue: String,
    val components: List<StaffComponent> = emptyList()
)


// ============================================================
// PAYMENT DATA
// ============================================================

data class StaffPayment(
    val id: String,

    // Unique device ID
    val deviceId: String,

    val customer: String,
    val device: String,
    val appointmentId: String,
    val amount: String,
    var status: String,
    val date: String
)


// ============================================================
// APPOINTMENT DATA
// ============================================================

data class StaffAppointment(
    val id: String,
    val customer: String,
    val device: String,
    val deviceId: String,
    val company: String,
    val date: String,
    val time: String,
    val location: String,
    var status: String
)


// ============================================================
// STAFF DATA STORE
// ============================================================

object StaffDataStore {


    // ========================================================
    // DEVICES
    // ========================================================

    val devices =
        mutableStateListOf(

            StaffDevice(
                id = "DEV001",
                deviceName = "iPhone 15",
                category = "Smartphone",
                customer = "Alex Tan",
                date = "22 Aug 2026",
                status = "Approved",
                recoveryValue = "RM 350",

                components =
                    listOf(

                        StaffComponent(
                            name = "Battery",
                            condition = "Good",
                            price = "RM 35",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "LCD / Display",
                            condition = "Good",
                            price = "RM 100",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Camera Module",
                            condition = "Good",
                            price = "RM 80",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Copper / Electronics",
                            condition = "Fair",
                            price = "RM 25",
                            status = "Recyclable"
                        )
                    )
            ),


            StaffDevice(
                id = "DEV002",
                deviceName = "Dell Inspiron",
                category = "Laptop",
                customer = "Alex Tan",
                date = "21 Aug 2026",
                status = "Approved",
                recoveryValue = "RM 280",

                components =
                    listOf(

                        StaffComponent(
                            name = "RAM 8GB",
                            condition = "Good",
                            price = "RM 40",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "256GB SSD",
                            condition = "Good",
                            price = "RM 80",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Laptop Battery",
                            condition = "Fair",
                            price = "RM 35",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "LCD Screen",
                            condition = "Good",
                            price = "RM 100",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Copper Wire",
                            condition = "Good",
                            price = "RM 25",
                            status = "Recyclable"
                        )
                    )
            ),


            StaffDevice(
                id = "DEV003",
                deviceName = "Mechanical Keyboard",
                category = "Keyboard",
                customer = "Daniel Lim",
                date = "20 Aug 2026",
                status = "Rejected",
                recoveryValue = "RM 40",

                components =
                    listOf(

                        StaffComponent(
                            name = "Mechanical Switches",
                            condition = "Poor",
                            price = "RM 15",
                            status = "Not Sellable"
                        ),

                        StaffComponent(
                            name = "Keycaps",
                            condition = "Good",
                            price = "RM 10",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "PCB",
                            condition = "Fair",
                            price = "RM 15",
                            status = "Recyclable"
                        )
                    )
            ),


            StaffDevice(
                id = "DEV004",
                deviceName = "Samsung Refrigerator",
                category = "Refrigerator",
                customer = "Jason Wong",
                date = "20 Aug 2026",
                status = "Completed",
                recoveryValue = "RM 520",

                components =
                    listOf(

                        StaffComponent(
                            name = "Copper Wire",
                            condition = "Good",
                            price = "RM 120",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Compressor",
                            condition = "Good",
                            price = "RM 180",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Aluminium",
                            condition = "Good",
                            price = "RM 100",
                            status = "Recyclable"
                        ),

                        StaffComponent(
                            name = "Control Board",
                            condition = "Fair",
                            price = "RM 120",
                            status = "Recyclable"
                        )
                    )
            ),


            StaffDevice(
                id = "DEV005",
                deviceName = "MacBook Air",
                category = "Laptop",
                customer = "Emily Tan",
                date = "19 Aug 2026",
                status = "Approved",
                recoveryValue = "RM 600",

                components =
                    listOf(

                        StaffComponent(
                            name = "RAM",
                            condition = "Good",
                            price = "RM 100",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "SSD",
                            condition = "Good",
                            price = "RM 150",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Battery",
                            condition = "Good",
                            price = "RM 80",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "LCD Screen",
                            condition = "Good",
                            price = "RM 220",
                            status = "Sellable"
                        ),

                        StaffComponent(
                            name = "Copper / Electronics",
                            condition = "Fair",
                            price = "RM 50",
                            status = "Recyclable"
                        )
                    )
            )
        )


    // ========================================================
    // PAYMENTS
    // ========================================================

    val payments =
        mutableStateListOf(

            StaffPayment(
                id = "PAY001",
                deviceId = "DEV001",
                customer = "Alex Tan",
                device = "iPhone 15",
                appointmentId = "",
                amount = "RM 350",
                status = "Pending",
                date = "22 Aug 2026"
            ),


            StaffPayment(
                id = "PAY002",
                deviceId = "DEV002",
                customer = "Sarah Lee",
                device = "Dell Inspiron",
                appointmentId = "APP001",
                amount = "RM 280",
                status = "Pending",
                date = "21 Aug 2026"
            ),


            StaffPayment(
                id = "PAY003",
                deviceId = "DEV004",
                customer = "Jason Wong",
                device = "Samsung Refrigerator",
                appointmentId = "APP002",
                amount = "RM 520",
                status = "Paid",
                date = "20 Aug 2026"
            ),


            StaffPayment(
                id = "PAY004",
                deviceId = "DEV005",
                customer = "Emily Tan",
                device = "MacBook Air",
                appointmentId = "APP003",
                amount = "RM 600",
                status = "Pending",
                date = "19 Aug 2026"
            )
        )


    // ========================================================
    // APPOINTMENTS
    // ========================================================

    val appointments =
        mutableStateListOf(

            StaffAppointment(
                id = "APP001",
                customer = "Sarah Lee",
                device = "Dell Inspiron",
                deviceId = "DEV002",
                company = "NekoEco Recycling",
                location = "Customer Address",
                date = "22 Aug 2026",
                time = "2:30 PM",
                status = "Scheduled"
            ),


            StaffAppointment(
                id = "APP002",
                customer = "Jason Wong",
                device = "Samsung Refrigerator",
                deviceId = "DEV004",
                company = "NekoEco Recycling",
                location = "Customer Address",
                date = "20 Aug 2026",
                time = "3:00 PM",
                status = "Completed"
            ),


            StaffAppointment(
                id = "APP003",
                customer = "Emily Tan",
                device = "MacBook Air",
                deviceId = "DEV005",
                company = "NekoEco Recycling",
                location = "Customer Address",
                date = "24 Aug 2026",
                time = "4:00 PM",
                status = "Cancelled"
            )
        )


    // ========================================================
    // ADD DEVICE FROM CUSTOMER
    // ========================================================
    fun addDeviceFromCustomer(
        device: CustomerDevice,
        customerName: String
    ) {

        if (
            devices.any {
                it.id == device.deviceId
            }
        ) {
            return
        }


        val components =
            device.accessories.map { accessory ->

                StaffComponent(
                    name = accessory.name,
                    condition = "Pending",
                    price =
                        "RM %.0f".format(
                            accessory.price
                        ),
                    status = "Pending"
                )
            }


        val staffDevice =
            StaffDevice(
                id = device.deviceId,
                deviceName = device.deviceName,
                category = device.category,
                customer = customerName,
                date = device.date,

                status =
                    convertCustomerStatusToStaff(
                        device.status
                    ),

                recoveryValue =
                    "RM %.0f".format(
                        device.recoveryValue
                    ),

                components = components
            )


        devices.add(
            staffDevice
        )


        if (
            staffDevice.status == "Approved"
        ) {

            createAppointmentForApprovedDevice(
                staffDevice
            )
        }
    }
    // ========================================================
// ADD APPOINTMENT FROM CUSTOMER
// ========================================================

    fun addAppointmentFromCustomer(
        appointment: CustomerAppointment
    ) {

        val customer =
            CustomerDataStore.currentCustomer

        val customerName =
            customer?.name ?: "Customer"


        val staffAppointment =
            StaffAppointment(

                id =
                    appointment.appointmentId,

                customer =
                    customerName,

                device =
                    appointment.deviceName,

                deviceId =
                    appointment.deviceId,

                company =
                    appointment.company,

                date =
                    appointment.date,

                time =
                    appointment.time,

                location =
                    appointment.location,

                status =
                    appointment.status
            )


        val existingIndex =
            appointments.indexOfFirst {

                it.id ==
                        appointment.appointmentId
            }


        // ========================================================
        // UPDATE EXISTING
        // ========================================================

        if (
            existingIndex != -1
        ) {

            appointments[existingIndex] =
                staffAppointment

            return
        }


        // ========================================================
        // ADD NEW
        // ========================================================

        appointments.add(
            staffAppointment
        )
    }

    // ========================================================
    // CUSTOMER STATUS -> STAFF STATUS
    // ========================================================

    private fun convertCustomerStatusToStaff(
        status: String
    ): String {

        return when (status) {

            "Scheduled" ->
                "Approved"

            "Completed" ->
                "Completed"

            "Rejected" ->
                "Rejected"

            "Approved" ->
                "Approved"

            else ->
                "Pending"
        }
    }


    // ========================================================
    // UPDATE DEVICE STATUS
    // ========================================================

    fun updateDeviceStatus(
        deviceId: String,
        newStatus: String
    ) {

        val index =
            devices.indexOfFirst {
                it.id == deviceId
            }


        if (index == -1) {
            return
        }


        if (
            newStatus != "Pending" &&
            newStatus != "Approved" &&
            newStatus != "Rejected"
        ) {
            return
        }


        val updatedDevice =
            devices[index].copy(
                status = newStatus
            )


        devices[index] =
            updatedDevice


        // ====================================================
        // SYNC CUSTOMER
        // ====================================================

        CustomerDataStore.updateDeviceStatus(
            deviceId = deviceId,
            newStatus = newStatus
        )


        // ====================================================
        // APPROVED
        // ====================================================

        if (
            newStatus == "Approved"
        ) {

            createAppointmentForApprovedDevice(
                updatedDevice
            )

            return
        }


        // ====================================================
        // REJECTED
        // ====================================================

        if (
            newStatus == "Rejected"
        ) {

            appointments
                .filter {
                    it.deviceId == deviceId &&
                            it.status != "Completed"
                }
                .toList()
                .forEach { appointment ->

                    val appointmentIndex =
                        appointments.indexOfFirst {
                            it.id == appointment.id
                        }


                    if (
                        appointmentIndex != -1
                    ) {

                        appointments[appointmentIndex] =
                            appointments[appointmentIndex]
                                .copy(
                                    status = "Cancelled"
                                )


                        CustomerDataStore
                            .updateAppointmentFromStaff(
                                appointmentId =
                                    appointment.id,

                                newDate =
                                    appointment.date,

                                newTime =
                                    appointment.time,

                                newStatus =
                                    "Cancelled"
                            )
                    }
                }


            updatePaymentStatusForDevice(
                deviceId = deviceId,
                newStatus = "Pending"
            )

            return
        }


        // ====================================================
        // PENDING
        // ====================================================

        if (
            newStatus == "Pending"
        ) {

            appointments
                .filter {
                    it.deviceId == deviceId &&
                            it.status != "Completed"
                }
                .toList()
                .forEach { appointment ->

                    val appointmentIndex =
                        appointments.indexOfFirst {
                            it.id == appointment.id
                        }


                    if (
                        appointmentIndex != -1
                    ) {

                        appointments[appointmentIndex] =
                            appointments[appointmentIndex]
                                .copy(
                                    status = "Cancelled"
                                )


                        CustomerDataStore
                            .updateAppointmentFromStaff(
                                appointmentId =
                                    appointment.id,

                                newDate =
                                    appointment.date,

                                newTime =
                                    appointment.time,

                                newStatus =
                                    "Cancelled"
                            )
                    }
                }


            updatePaymentStatusForDevice(
                deviceId = deviceId,
                newStatus = "Pending"
            )
        }
    }


    // ========================================================
    // CREATE APPOINTMENT
    // ========================================================

    private fun createAppointmentForApprovedDevice(
        device: StaffDevice
    ) {

        // ====================================================
        // ACTIVE APPOINTMENT EXISTS
        // ====================================================

        val activeAppointment =
            appointments.find {

                it.deviceId == device.id &&
                        (
                                it.status == "Scheduled" ||
                                        it.status == "Pending"
                                )
            }


        if (
            activeAppointment != null
        ) {

            updatePaymentForAppointment(
                activeAppointment.id
            )


            CustomerDataStore
                .updateAppointmentFromStaff(
                    appointmentId =
                        activeAppointment.id,

                    newDate =
                        activeAppointment.date,

                    newTime =
                        activeAppointment.time,

                    newStatus =
                        "Scheduled"
                )


            syncCustomerScheduled(
                device.id
            )

            return
        }


        // ====================================================
        // REUSE CANCELLED APPOINTMENT
        // ====================================================

        val cancelledIndex =
            appointments.indexOfFirst {

                it.deviceId == device.id &&
                        it.status == "Cancelled"
            }


        if (
            cancelledIndex != -1
        ) {

            val cancelledAppointment =
                appointments[cancelledIndex]


            val scheduledAppointment =
                cancelledAppointment.copy(
                    status = "Scheduled"
                )


            appointments[cancelledIndex] =
                scheduledAppointment


            CustomerDataStore
                .updateAppointmentFromStaff(
                    appointmentId =
                        scheduledAppointment.id,

                    newDate =
                        scheduledAppointment.date,

                    newTime =
                        scheduledAppointment.time,

                    newStatus =
                        "Scheduled"
                )


            updatePaymentForAppointment(
                scheduledAppointment.id
            )


            syncCustomerScheduled(
                device.id
            )

            return
        }


        // ====================================================
        // CREATE NEW APPOINTMENT
        // ====================================================

        val newId =
            generateAppointmentId()


        val newAppointment =
            StaffAppointment(
                id = newId,
                customer = device.customer,
                device = device.deviceName,
                deviceId = device.id,
                company = "NekoEco Recycling",
                location = "Customer Address",
                date = device.date,
                time = "10:00 AM",
                status = "Scheduled"
            )


        appointments.add(
            newAppointment
        )


        // ====================================================
        // CUSTOMER APPOINTMENT
        // ====================================================

        val customerDevice =
            CustomerDataStore.devices.find {
                it.deviceId == device.id
            }


        if (
            customerDevice != null
        ) {

            CustomerDataStore
                .addAppointmentFromStaff(

                    CustomerAppointment(
                        appointmentId =
                            newAppointment.id,

                        customerId =
                            customerDevice.customerId,

                        deviceId =
                            device.id,

                        deviceName =
                            device.deviceName,

                        date =
                            device.date,

                        time =
                            "10:00 AM",

                        company =
                            "NekoEco Recycling",

                        location =
                            "Customer Address",

                        status =
                            "Scheduled"
                    )
                )
        }


        // ====================================================
        // PAYMENT
        // ====================================================

        createPaymentForDevice(
            device = device,
            appointmentId = newId
        )


        syncCustomerScheduled(
            device.id
        )
    }


    // ========================================================
    // GENERATE APPOINTMENT ID
    // ========================================================

    private fun generateAppointmentId(): String {

        var number =
            appointments.size + 1


        var id =
            "APP${
                number
                    .toString()
                    .padStart(
                        3,
                        '0'
                    )
            }"


        while (
            appointments.any {
                it.id == id
            }
        ) {

            number++


            id =
                "APP${
                    number
                        .toString()
                        .padStart(
                            3,
                            '0'
                        )
                }"
        }


        return id
    }


    // ========================================================
    // GENERATE PAYMENT ID
    // ========================================================

    private fun generatePaymentId(): String {

        var number =
            payments.size + 1


        var id =
            "PAY${
                number
                    .toString()
                    .padStart(
                        3,
                        '0'
                    )
            }"


        while (
            payments.any {
                it.id == id
            }
        ) {

            number++


            id =
                "PAY${
                    number
                        .toString()
                        .padStart(
                            3,
                            '0'
                        )
                }"
        }


        return id
    }


    // ========================================================
    // CREATE PAYMENT
    // ========================================================

    private fun createPaymentForDevice(
        device: StaffDevice,
        appointmentId: String
    ) {

        // ====================================================
        // IMPORTANT:
        // Match using DEVICE ID, not device name.
        // ====================================================

        val paymentExists =
            payments.any {
                it.deviceId == device.id
            }


        if (
            paymentExists
        ) {

            updatePaymentForAppointment(
                appointmentId
            )

            return
        }


        val newPaymentId =
            generatePaymentId()


        // ====================================================
        // STAFF PAYMENT
        // ====================================================

        payments.add(

            StaffPayment(
                id = newPaymentId,

                deviceId =
                    device.id,

                customer =
                    device.customer,

                device =
                    device.deviceName,

                appointmentId =
                    appointmentId,

                amount =
                    device.recoveryValue,

                status =
                    "Pending",

                date =
                    device.date
            )
        )


        // ====================================================
        // CUSTOMER PAYMENT
        // ====================================================

        val customerDevice =
            CustomerDataStore.devices.find {
                it.deviceId == device.id
            }


        if (
            customerDevice != null
        ) {

            CustomerDataStore
                .addPaymentFromStaff(

                    CustomerPayment(
                        paymentId =
                            newPaymentId,

                        customerId =
                            customerDevice.customerId,

                        appointmentId =
                            appointmentId,

                        deviceName =
                            device.deviceName,

                        amount =
                            device.recoveryValue
                                .replace(
                                    "RM",
                                    ""
                                )
                                .replace(
                                    ",",
                                    ""
                                )
                                .trim()
                                .toDoubleOrNull()
                                ?: 0.0,

                        date =
                            device.date,

                        method =
                            "Bank Transfer",

                        status =
                            "Pending"
                    )
                )
        }
    }


    // ========================================================
    // UPDATE PAYMENT APPOINTMENT
    // ========================================================

    private fun updatePaymentForAppointment(
        appointmentId: String
    ) {

        val appointment =
            appointments.find {
                it.id == appointmentId
            } ?: return


        // ====================================================
        // MATCH BY DEVICE ID
        // ====================================================

        val paymentIndex =
            payments.indexOfFirst {
                it.deviceId ==
                        appointment.deviceId
            }


        if (
            paymentIndex != -1
        ) {

            payments[paymentIndex] =
                payments[paymentIndex]
                    .copy(
                        appointmentId =
                            appointmentId,

                        status =
                            "Pending"
                    )


            CustomerDataStore
                .updatePaymentStatusFromStaff(
                    paymentId =
                        payments[paymentIndex].id,

                    newStatus =
                        "Pending"
                )

        } else {

            val device =
                devices.find {
                    it.id ==
                            appointment.deviceId
                }


            if (
                device != null
            ) {

                createPaymentForDevice(
                    device = device,
                    appointmentId =
                        appointmentId
                )
            }
        }
    }


    // ========================================================
    // UPDATE PAYMENT STATUS BY DEVICE
    // ========================================================

    private fun updatePaymentStatusForDevice(
        deviceId: String,
        newStatus: String
    ) {

        // ====================================================
        // MATCH BY DEVICE ID
        // ====================================================

        payments
            .filter {
                it.deviceId == deviceId
            }
            .toList()
            .forEach { payment ->

                val paymentIndex =
                    payments.indexOfFirst {
                        it.id == payment.id
                    }


                if (
                    paymentIndex != -1
                ) {

                    payments[paymentIndex] =
                        payments[paymentIndex]
                            .copy(
                                status = newStatus
                            )


                    CustomerDataStore
                        .updatePaymentStatusFromStaff(
                            paymentId =
                                payment.id,

                            newStatus =
                                newStatus
                        )
                }
            }
    }


    // ========================================================
    // SYNC CUSTOMER -> SCHEDULED
    // ========================================================

    private fun syncCustomerScheduled(
        deviceId: String
    ) {

        CustomerDataStore.updateDeviceStatus(
            deviceId = deviceId,
            newStatus = "Scheduled"
        )
    }


    // ========================================================
    // DELETE DEVICE
    // STAFF SIDE ONLY
    // ========================================================

    fun deleteDevice(
        id: String
    ) {

        val deviceExists =
            devices.any {
                it.id == id
            }


        if (
            !deviceExists
        ) {
            return
        }


        appointments.removeAll {
            it.deviceId == id
        }


        // ====================================================
        // MATCH PAYMENT BY DEVICE ID
        // ====================================================

        payments.removeAll {
            it.deviceId == id
        }


        devices.removeAll {
            it.id == id
        }
    }


    // ========================================================
    // MARK PAYMENT PAID
    // ========================================================

    fun markPaymentPaid(
        paymentId: String
    ) {

        val index =
            payments.indexOfFirst {
                it.id == paymentId
            }


        if (
            index == -1
        ) {
            return
        }


        payments[index] =
            payments[index].copy(
                status = "Paid"
            )


        CustomerDataStore
            .updatePaymentStatusFromStaff(
                paymentId = paymentId,
                newStatus = "Paid"
            )
    }


    // ========================================================
    // ACCEPT APPOINTMENT
    // ========================================================

    fun acceptAppointment(
        id: String
    ) {

        val appointmentIndex =
            appointments.indexOfFirst {
                it.id == id
            }


        if (
            appointmentIndex == -1
        ) {
            return
        }


        val appointment =
            appointments[appointmentIndex]


        val deviceIndex =
            devices.indexOfFirst {
                it.id == appointment.deviceId
            }


        if (
            deviceIndex == -1
        ) {
            return
        }


        if (
            devices[deviceIndex].status ==
            "Rejected"
        ) {
            return
        }


        appointments[appointmentIndex] =
            appointment.copy(
                status = "Scheduled"
            )


        CustomerDataStore
            .updateAppointmentFromStaff(
                appointmentId = id,
                newDate = appointment.date,
                newTime = appointment.time,
                newStatus = "Scheduled"
            )


        if (
            devices[deviceIndex].status !=
            "Completed"
        ) {

            devices[deviceIndex] =
                devices[deviceIndex]
                    .copy(
                        status = "Approved"
                    )
        }


        CustomerDataStore
            .updateDeviceStatus(
                deviceId =
                    appointment.deviceId,

                newStatus =
                    "Scheduled"
            )


        updatePaymentForAppointment(
            id
        )
    }


    // ========================================================
    // REJECT APPOINTMENT
    // ========================================================

    fun rejectAppointment(
        id: String
    ) {

        val appointmentIndex =
            appointments.indexOfFirst {
                it.id == id
            }


        if (
            appointmentIndex == -1
        ) {
            return
        }


        val appointment =
            appointments[appointmentIndex]


        appointments[appointmentIndex] =
            appointment.copy(
                status = "Cancelled"
            )


        CustomerDataStore
            .updateAppointmentFromStaff(
                appointmentId = id,
                newDate = appointment.date,
                newTime = appointment.time,
                newStatus = "Cancelled"
            )


        val deviceIndex =
            devices.indexOfFirst {
                it.id == appointment.deviceId
            }


        if (
            deviceIndex != -1
        ) {

            devices[deviceIndex] =
                devices[deviceIndex]
                    .copy(
                        status = "Rejected"
                    )
        }


        CustomerDataStore
            .updateDeviceStatus(
                deviceId =
                    appointment.deviceId,

                newStatus =
                    "Rejected"
            )


        updatePaymentStatusForDevice(
            deviceId =
                appointment.deviceId,

            newStatus =
                "Pending"
        )
    }


    // ========================================================
    // COMPLETE APPOINTMENT
    // ========================================================

    fun completeAppointment(
        id: String
    ) {

        val appointmentIndex =
            appointments.indexOfFirst {
                it.id == id
            }


        if (
            appointmentIndex == -1
        ) {
            return
        }


        val appointment =
            appointments[appointmentIndex]


        if (
            appointment.status !=
            "Scheduled"
        ) {
            return
        }


        val deviceIndex =
            devices.indexOfFirst {
                it.id == appointment.deviceId
            }


        if (
            deviceIndex == -1
        ) {
            return
        }


        if (
            devices[deviceIndex].status ==
            "Rejected"
        ) {
            return
        }


        // ====================================================
        // APPOINTMENT COMPLETED
        // ====================================================

        appointments[appointmentIndex] =
            appointment.copy(
                status = "Completed"
            )


        CustomerDataStore
            .updateAppointmentFromStaff(
                appointmentId = id,
                newDate = appointment.date,
                newTime = appointment.time,
                newStatus = "Completed"
            )


        // ====================================================
        // DEVICE COMPLETED
        // ====================================================

        devices[deviceIndex] =
            devices[deviceIndex]
                .copy(
                    status = "Completed"
                )


        CustomerDataStore
            .updateDeviceStatus(
                deviceId =
                    appointment.deviceId,

                newStatus =
                    "Completed"
            )


        // ====================================================
        // PAYMENT PAID
        // ====================================================

        val paymentIndex =
            payments.indexOfFirst {

                it.deviceId ==
                        appointment.deviceId
            }


        if (
            paymentIndex != -1
        ) {

            val paymentId =
                payments[paymentIndex].id


            payments[paymentIndex] =
                payments[paymentIndex]
                    .copy(
                        status = "Paid"
                    )


            CustomerDataStore
                .updatePaymentStatusFromStaff(
                    paymentId = paymentId,
                    newStatus = "Paid"
                )
        }
    }


    // ========================================================
    // RESCHEDULE APPOINTMENT
    // ========================================================

    fun rescheduleAppointment(
        id: String,
        newDate: String,
        newTime: String
    ) {

        val appointmentIndex =
            appointments.indexOfFirst {
                it.id == id
            }


        if (
            appointmentIndex == -1
        ) {
            return
        }


        val appointment =
            appointments[appointmentIndex]


        if (
            appointment.status != "Cancelled" &&
            appointment.status != "Scheduled"
        ) {
            return
        }


        val deviceIndex =
            devices.indexOfFirst {
                it.id == appointment.deviceId
            }


        if (
            deviceIndex == -1
        ) {
            return
        }


        if (
            devices[deviceIndex].status !=
            "Approved"
        ) {
            return
        }


        appointments[appointmentIndex] =
            appointment.copy(
                date = newDate,
                time = newTime,
                status = "Scheduled"
            )


        CustomerDataStore
            .updateAppointmentFromStaff(
                appointmentId = id,
                newDate = newDate,
                newTime = newTime,
                newStatus = "Scheduled"
            )


        CustomerDataStore
            .updateDeviceStatus(
                deviceId =
                    appointment.deviceId,

                newStatus =
                    "Scheduled"
            )


        updatePaymentForAppointment(
            id
        )
    }


    // ========================================================
    // CUSTOMER CANCEL APPOINTMENT
    // ========================================================

    fun cancelAppointmentByCustomer(
        appointmentId: String
    ) {

        val appointmentIndex =
            appointments.indexOfFirst {
                it.id == appointmentId
            }


        if (
            appointmentIndex == -1
        ) {
            return
        }


        val appointment =
            appointments[appointmentIndex]


        if (
            appointment.status !=
            "Scheduled"
        ) {
            return
        }


        appointments[appointmentIndex] =
            appointment.copy(
                status = "Cancelled"
            )


        val deviceIndex =
            devices.indexOfFirst {
                it.id ==
                        appointment.deviceId
            }


        if (
            deviceIndex != -1
        ) {

            if (
                devices[deviceIndex].status !=
                "Rejected" &&

                devices[deviceIndex].status !=
                "Completed"
            ) {

                devices[deviceIndex] =
                    devices[deviceIndex]
                        .copy(
                            status = "Approved"
                        )
            }
        }


        updatePaymentStatusForDevice(
            deviceId =
                appointment.deviceId,

            newStatus =
                "Pending"
        )
    }


    // ========================================================
    // UPDATE DEVICE NAME FROM CUSTOMER
    // ========================================================

    fun updateDeviceNameFromCustomer(
        deviceId: String,
        newName: String
    ) {

        val deviceIndex =
            devices.indexOfFirst {
                it.id == deviceId
            }


        if (
            deviceIndex == -1
        ) {
            return
        }


        val oldDevice =
            devices[deviceIndex]


        // ====================================================
        // DEVICE
        // ====================================================

        devices[deviceIndex] =
            oldDevice.copy(
                deviceName = newName
            )


        // ====================================================
        // APPOINTMENTS
        // ====================================================

        appointments
            .mapIndexedNotNull { index, appointment ->

                if (
                    appointment.deviceId ==
                    deviceId
                ) {

                    index

                } else {

                    null
                }
            }
            .forEach { index ->

                appointments[index] =
                    appointments[index]
                        .copy(
                            device = newName
                        )
            }


        // ====================================================
        // PAYMENTS
        // MATCH BY DEVICE ID
        // ====================================================

        payments
            .mapIndexedNotNull { index, payment ->

                if (
                    payment.deviceId ==
                    deviceId
                ) {

                    index

                } else {

                    null
                }
            }
            .forEach { index ->

                payments[index] =
                    payments[index]
                        .copy(
                            device = newName
                        )
            }
    }


    // ========================================================
    // DELETE DEVICE FROM CUSTOMER
    // ========================================================

    fun deleteDeviceFromCustomer(
        deviceId: String
    ) {

        val deviceExists =
            devices.any {
                it.id == deviceId
            }


        if (
            !deviceExists
        ) {
            return
        }


        appointments.removeAll {
            it.deviceId ==
                    deviceId
        }


        // ====================================================
        // MATCH PAYMENT BY DEVICE ID
        // ====================================================

        payments.removeAll {
            it.deviceId ==
                    deviceId
        }


        devices.removeAll {
            it.id ==
                    deviceId
        }
    }
}