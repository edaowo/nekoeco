package com.example.nekoeco.ui.customer

import androidx.compose.runtime.mutableStateListOf
import com.example.nekoeco.ui.staff.StaffDataStore


// ============================================================
// CUSTOMER DEVICE
// ============================================================

data class CustomerDevice(
    val deviceId: String,
    val customerId: String,
    val deviceName: String,
    val category: String,
    val date: String,
    var status: String,
    val accessories: List<CustomerAccessory>,
    val recoveryValue: Double
)


// ============================================================
// ACCESSORY
// ============================================================

data class CustomerAccessory(
    val name: String,
    val price: Double
)


// ============================================================
// CUSTOMER
// ============================================================

data class Customer(
    val customerId: String,
    val name: String,
    val email: String,
    val password: String
)


// ============================================================
// CUSTOMER APPOINTMENT
// ============================================================

data class CustomerAppointment(
    val appointmentId: String,
    val customerId: String,
    val deviceId: String,
    val deviceName: String,
    val date: String,
    val time: String,
    val company: String,
    val location: String,
    var status: String
)


// ============================================================
// CUSTOMER PAYMENT
// ============================================================

data class CustomerPayment(
    val paymentId: String,
    val customerId: String,
    val appointmentId: String,
    val deviceName: String,
    val amount: Double,
    val date: String,
    val method: String,
    var status: String
)


// ============================================================
// CUSTOMER DATA STORE
// ============================================================

object CustomerDataStore {


    // ========================================================
    // CUSTOMER ACCOUNTS
    // ========================================================

    private val accounts =
        mutableListOf(

            Customer(
                customerId = "C001",
                name = "Alex Tan",
                email = "alex@gmail.com",
                password = "123456"
            )
        )


    // ========================================================
    // CURRENT CUSTOMER
    // ========================================================

    var currentCustomer: Customer? = null


    // ========================================================
    // LOGIN
    // ========================================================

    fun login(
        email: String,
        password: String
    ): Customer? {

        val customer =
            accounts.find {

                it.email.equals(
                    email.trim(),
                    ignoreCase = true
                ) &&
                        it.password == password
            }

        currentCustomer =
            customer

        return customer
    }


    // ========================================================
    // SIGN UP
    // ========================================================

    fun signUp(
        name: String,
        email: String,
        password: String
    ): Boolean {

        val cleanName =
            name.trim()

        val cleanEmail =
            email.trim()


        if (
            cleanName.isBlank() ||
            cleanEmail.isBlank() ||
            password.isBlank()
        ) {
            return false
        }


        val exists =
            accounts.any {

                it.email.equals(
                    cleanEmail,
                    ignoreCase = true
                )
            }


        if (exists) {
            return false
        }


        val customer =
            Customer(

                customerId =
                    generateCustomerId(),

                name =
                    cleanName,

                email =
                    cleanEmail,

                password =
                    password
            )


        accounts.add(
            customer
        )

        currentCustomer =
            customer

        return true
    }


    // ========================================================
    // GENERATE CUSTOMER ID
    // ========================================================

    private fun generateCustomerId(): String {

        val maxNumber =
            accounts
                .mapNotNull {

                    it.customerId
                        .removePrefix("C")
                        .toIntOrNull()
                }
                .maxOrNull()
                ?: 0


        return "C" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }


    // ========================================================
    // LOGOUT
    // ========================================================

    fun logout() {

        currentCustomer =
            null
    }


    // ========================================================
    // CUSTOMER DEVICES
    // ========================================================

    val devices =
        mutableStateListOf(

            CustomerDevice(

                deviceId = "DEV001",

                customerId = "C001",

                deviceName = "iPhone 15",

                category = "Smartphone",

                date = "22 Aug 2026",

                status = "Approved",

                accessories =
                    listOf(

                        CustomerAccessory(
                            name = "Battery",
                            price = 80.00
                        ),

                        CustomerAccessory(
                            name = "LCD Screen",
                            price = 150.00
                        ),

                        CustomerAccessory(
                            name = "Copper Components",
                            price = 120.00
                        )
                    ),

                recoveryValue =
                    350.00
            ),


            CustomerDevice(

                deviceId = "DEV002",

                customerId = "C001",

                deviceName = "Dell Inspiron",

                category = "Laptop",

                date = "21 Aug 2026",

                status = "Scheduled",

                accessories =
                    listOf(

                        CustomerAccessory(
                            name = "RAM 8GB",
                            price = 40.00
                        ),

                        CustomerAccessory(
                            name = "256GB SSD",
                            price = 80.00
                        ),

                        CustomerAccessory(
                            name = "Laptop Battery",
                            price = 35.00
                        ),

                        CustomerAccessory(
                            name = "LCD Screen",
                            price = 125.00
                        )
                    ),

                recoveryValue =
                    280.00
            )
        )


    // ========================================================
    // CUSTOMER APPOINTMENTS
    // ========================================================

    val appointments =
        mutableStateListOf(

            CustomerAppointment(

                appointmentId = "APP001",

                customerId = "C001",

                deviceId = "DEV002",

                deviceName = "Dell Inspiron",

                date = "22 Aug 2026",

                time = "2:30 PM",

                company = "NekoEco Recycling",

                location = "Customer Address",

                status = "Scheduled"
            )
        )


    // ========================================================
    // CUSTOMER PAYMENTS
    // ========================================================

    val payments =
        mutableStateListOf(

            CustomerPayment(

                paymentId = "PAY001",

                customerId = "C001",

                appointmentId = "APP001",

                deviceName = "Dell Inspiron",

                amount = 280.00,

                date = "22 Aug 2026",

                method = "Bank Transfer",

                status = "Pending"
            )
        )


    // ========================================================
    // GET CURRENT CUSTOMER DEVICES
    // ========================================================

    fun getCurrentCustomerDevices():
            List<CustomerDevice> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return devices.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // GET CURRENT CUSTOMER APPOINTMENTS
    // ========================================================

    fun getCurrentCustomerAppointments():
            List<CustomerAppointment> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return appointments.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // GET CURRENT CUSTOMER PAYMENTS
    // ========================================================

    fun getCurrentCustomerPayments():
            List<CustomerPayment> {

        val customerId =
            currentCustomer?.customerId
                ?: return emptyList()


        return payments.filter {

            it.customerId ==
                    customerId
        }
    }


    // ========================================================
    // ADD DEVICE
    // ========================================================

    fun addDevice(
        device: CustomerDevice
    ) {

        val customer =
            currentCustomer
                ?: return


        val customerDevice =

            if (
                device.customerId ==
                customer.customerId
            ) {

                device

            } else {

                device.copy(
                    customerId =
                        customer.customerId
                )
            }


        if (
            devices.any {

                it.deviceId ==
                        customerDevice.deviceId
            }
        ) {

            return
        }


        devices.add(
            customerDevice
        )


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore.addDeviceFromCustomer(

            device =
                customerDevice,

            customerName =
                customer.name
        )
    }


    // ========================================================
    // DELETE DEVICE
    //
    // Keep this function for older parts of the app.
    // It uses the same Pending / Rejected rule.
    // ========================================================

    fun deleteDevice(
        deviceId: String
    ) {

        deleteDeviceByCustomer(
            deviceId
        )
    }


    // ========================================================
    // DELETE DEVICE BY CUSTOMER
    //
    // Only Pending / Rejected devices can be deleted.
    //
    // Customer deletion also removes the Staff copy.
    // ========================================================

    fun deleteDeviceByCustomer(
        deviceId: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val device =
            devices.find {

                it.deviceId ==
                        deviceId &&

                        it.customerId ==
                        customerId

            } ?: return


        // ====================================================
        // ONLY PENDING / REJECTED
        // ====================================================

        if (
            device.status != "Pending" &&
            device.status != "Rejected"
        ) {

            return
        }


        // ====================================================
        // GET APPOINTMENT IDS
        // ====================================================

        val removedAppointmentIds =

            appointments
                .filter {

                    it.deviceId ==
                            deviceId &&

                            it.customerId ==
                            customerId
                }
                .map {

                    it.appointmentId
                }


        // ====================================================
        // REMOVE CUSTOMER APPOINTMENTS
        // ====================================================

        appointments.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        // ====================================================
        // REMOVE CUSTOMER PAYMENTS
        // ====================================================

        payments.removeAll {

            it.customerId ==
                    customerId &&

                    (
                            it.appointmentId in
                                    removedAppointmentIds ||

                                    it.deviceName ==
                                    device.deviceName
                            )
        }


        // ====================================================
        // REMOVE CUSTOMER DEVICE
        // ====================================================

        devices.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        // ====================================================
        // REMOVE STAFF COPY
        // ====================================================

        StaffDataStore
            .deleteDeviceFromCustomer(
                deviceId
            )
    }


    // ========================================================
    // DELETE DEVICE FROM STAFF
    //
    // This function can remain for compatibility.
    // Staff delete itself should NOT call this anymore.
    // ========================================================

    fun deleteDeviceFromStaff(
        deviceId: String
    ) {

        val device =
            devices.find {

                it.deviceId ==
                        deviceId

            } ?: return


        val customerId =
            device.customerId


        val removedAppointmentIds =

            appointments
                .filter {

                    it.deviceId ==
                            deviceId &&

                            it.customerId ==
                            customerId
                }
                .map {

                    it.appointmentId
                }


        appointments.removeAll {

            it.deviceId ==
                    deviceId &&

                    it.customerId ==
                    customerId
        }


        payments.removeAll {

            it.customerId ==
                    customerId &&

                    it.appointmentId in
                    removedAppointmentIds
        }


        devices.remove(
            device
        )
    }


    // ========================================================
    // UPDATE DEVICE STATUS
    //
    // Used mainly by StaffDataStore.
    //
    // Does NOT call StaffDataStore again.
    // ========================================================

    fun updateDeviceStatus(
        deviceId: String,
        newStatus: String
    ) {

        val index =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId
            }


        if (
            index == -1
        ) {

            return
        }


        val customerId =
            devices[index]
                .customerId


        // ====================================================
        // UPDATE CUSTOMER DEVICE
        // ====================================================

        devices[index] =

            devices[index]
                .copy(
                    status =
                        newStatus
                )


        // ====================================================
        // UPDATE CUSTOMER APPOINTMENTS
        // ====================================================

        val appointmentIndexes =

            appointments
                .mapIndexedNotNull {
                        appointmentIndex,
                        appointment ->

                    if (
                        appointment.deviceId ==
                        deviceId &&

                        appointment.customerId ==
                        customerId
                    ) {

                        appointmentIndex

                    } else {

                        null
                    }
                }


        appointmentIndexes
            .forEach { appointmentIndex ->

                val appointment =
                    appointments[appointmentIndex]


                when (
                    newStatus
                ) {

                    "Scheduled" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Scheduled"
                            )
                    }


                    "Completed" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Completed"
                            )
                    }


                    "Rejected" -> {

                        appointments[appointmentIndex] =

                            appointment.copy(
                                status =
                                    "Cancelled"
                            )
                    }
                }
            }
    }


    // ========================================================
    // UPDATE DEVICE NAME
    // ========================================================

    fun updateDeviceName(
        deviceId: String,
        newName: String
    ) {

        val cleanName =
            newName.trim()


        if (
            cleanName.isBlank()
        ) {

            return
        }


        val deviceIndex =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId
            }


        if (
            deviceIndex == -1
        ) {

            return
        }


        val oldDevice =
            devices[deviceIndex]


        // ====================================================
        // COMPLETED DEVICE CANNOT BE EDITED
        // ====================================================

        if (
            oldDevice.status ==
            "Completed"
        ) {

            return
        }


        // ====================================================
        // UPDATE CUSTOMER DEVICE
        // ====================================================

        devices[deviceIndex] =

            oldDevice.copy(
                deviceName =
                    cleanName
            )


        // ====================================================
        // UPDATE CUSTOMER APPOINTMENTS
        // ====================================================

        appointments
            .mapIndexedNotNull {
                    index,
                    appointment ->

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
                            deviceName =
                                cleanName
                        )
            }


        // ====================================================
        // UPDATE CUSTOMER PAYMENTS
        // ====================================================

        payments
            .mapIndexedNotNull {
                    index,
                    payment ->

                if (
                    payment.customerId ==
                    oldDevice.customerId &&

                    payment.deviceName ==
                    oldDevice.deviceName
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
                            deviceName =
                                cleanName
                        )
            }


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore
            .updateDeviceNameFromCustomer(

                deviceId =
                    deviceId,

                newName =
                    cleanName
            )
    }


    // ========================================================
    // ADD APPOINTMENT
    // ========================================================
    fun addAppointment(
        appointment: CustomerAppointment
    ) {

        val customer =
            currentCustomer
                ?: return


        val customerAppointment =

            if (
                appointment.customerId ==
                customer.customerId
            ) {

                appointment

            } else {

                appointment.copy(
                    customerId =
                        customer.customerId
                )
            }


        // Prevent duplicate
        if (
            appointments.any {

                it.appointmentId ==
                        customerAppointment
                            .appointmentId
            }
        ) {

            return
        }

        appointments.add(
            customerAppointment
        )

        StaffDataStore.addAppointmentFromCustomer(
            customerAppointment
        )
    }

    // ========================================================
    // ADD APPOINTMENT FROM STAFF
    // ========================================================

    fun addAppointmentFromStaff(
        appointment: CustomerAppointment
    ) {

        if (
            appointments.any {

                it.appointmentId ==
                        appointment.appointmentId
            }
        ) {

            return
        }


        appointments.add(
            appointment
        )
    }


    // ========================================================
    // UPDATE APPOINTMENT STATUS
    // ========================================================

    fun updateAppointmentStatus(
        appointmentId: String,
        newStatus: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val index =
            appointments.indexOfFirst {

                it.appointmentId ==
                        appointmentId &&

                        it.customerId ==
                        customerId
            }


        if (
            index == -1
        ) {

            return
        }


        appointments[index] =

            appointments[index]
                .copy(
                    status =
                        newStatus
                )


        // ====================================================
        // SYNC DEVICE STATUS
        // ====================================================

        val deviceId =
            appointments[index]
                .deviceId


        val deviceIndex =
            devices.indexOfFirst {

                it.deviceId ==
                        deviceId &&

                        it.customerId ==
                        customerId
            }


        if (
            deviceIndex != -1
        ) {

            when (
                newStatus
            ) {

                "Scheduled" -> {

                    devices[deviceIndex] =

                        devices[deviceIndex]
                            .copy(
                                status =
                                    "Scheduled"
                            )
                }


                "Completed" -> {

                    devices[deviceIndex] =

                        devices[deviceIndex]
                            .copy(
                                status =
                                    "Completed"
                            )
                }


                "Cancelled" -> {

                    // Customer cancellation does not
                    // reject the device.

                    if (
                        devices[deviceIndex]
                            .status !=
                        "Rejected"
                    ) {

                        devices[deviceIndex] =

                            devices[deviceIndex]
                                .copy(
                                    status =
                                        "Approved"
                                )
                    }
                }
            }
        }
    }


    // ========================================================
    // UPDATE APPOINTMENT FROM STAFF
    // ========================================================

    fun updateAppointmentFromStaff(
        appointmentId: String,
        newDate: String,
        newTime: String,
        newStatus: String
    ) {

        val index =
            appointments.indexOfFirst {

                it.appointmentId ==
                        appointmentId
            }


        if (
            index == -1
        ) {

            return
        }


        appointments[index] =

            appointments[index]
                .copy(

                    date =
                        newDate,

                    time =
                        newTime,

                    status =
                        newStatus
                )
    }


    // ========================================================
    // CANCEL APPOINTMENT
    // ========================================================

    fun cancelAppointment(
        appointmentId: String
    ) {

        val customerId =
            currentCustomer?.customerId
                ?: return


        val appointment =
            appointments.find {

                it.appointmentId ==
                        appointmentId &&

                        it.customerId ==
                        customerId

            } ?: return


        // Only Scheduled can be cancelled
        if (
            appointment.status !=
            "Scheduled"
        ) {

            return
        }


        updateAppointmentStatus(

            appointmentId =
                appointmentId,

            newStatus =
                "Cancelled"
        )


        // ====================================================
        // SYNC STAFF
        // ====================================================

        StaffDataStore
            .cancelAppointmentByCustomer(

                appointmentId =
                    appointmentId
            )
    }


    // ========================================================
    // GENERATE APPOINTMENT ID
    // ========================================================

    fun generateAppointmentId():
            String {

        val maxNumber =
            appointments
                .mapNotNull {

                    it.appointmentId
                        .removePrefix("APP")
                        .toIntOrNull()
                }
                .maxOrNull()
                ?: 0


        return "APP" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }


    // ========================================================
    // ADD PAYMENT
    // ========================================================

    fun addPayment(
        payment: CustomerPayment
    ) {

        val customer =
            currentCustomer
                ?: return


        val customerPayment =

            if (
                payment.customerId ==
                customer.customerId
            ) {

                payment

            } else {

                payment.copy(
                    customerId =
                        customer.customerId
                )
            }


        if (
            payments.any {

                it.paymentId ==
                        customerPayment.paymentId
            }
        ) {

            return
        }


        payments.add(
            customerPayment
        )
    }


    // ========================================================
    // ADD PAYMENT FROM STAFF
    // ========================================================

    fun addPaymentFromStaff(
        payment: CustomerPayment
    ) {

        if (
            payments.any {

                it.paymentId ==
                        payment.paymentId
            }
        ) {

            return
        }


        payments.add(
            payment
        )
    }


    // ========================================================
    // UPDATE PAYMENT STATUS FROM STAFF
    // ========================================================

    fun updatePaymentStatusFromStaff(
        paymentId: String,
        newStatus: String
    ) {

        val index =
            payments.indexOfFirst {

                it.paymentId ==
                        paymentId
            }


        if (
            index == -1
        ) {

            return
        }


        payments[index] =

            payments[index]
                .copy(
                    status =
                        newStatus
                )
    }


    // ========================================================
    // GENERATE PAYMENT ID
    // ========================================================

    fun generatePaymentId():
            String {

        val maxNumber =
            payments
                .mapNotNull {

                    it.paymentId
                        .removePrefix("PAY")
                        .toIntOrNull()
                }
                .maxOrNull()
                ?: 0


        return "PAY" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }


    // ========================================================
    // GENERATE DEVICE ID
    // ========================================================

    fun generateDeviceId():
            String {

        val customerIds =

            devices.mapNotNull {

                it.deviceId
                    .removePrefix("DEV")
                    .toIntOrNull()
            }


        val staffIds =

            StaffDataStore
                .devices
                .mapNotNull {

                    it.id
                        .removePrefix("DEV")
                        .toIntOrNull()
                }


        val maxNumber =

            (customerIds + staffIds)
                .maxOrNull()
                ?: 0


        return "DEV" +
                (maxNumber + 1)
                    .toString()
                    .padStart(3, '0')
    }


    // ========================================================
    // TOTAL RECOVERY VALUE
    // ========================================================

    fun getTotalRecoveryValue():
            Double {

        return getCurrentCustomerDevices()
            .sumOf {

                it.recoveryValue
            }
    }


    // ========================================================
    // DEVICES SOLD
    // ========================================================

    fun getDevicesSold():
            Int {

        return getCurrentCustomerDevices()
            .count {

                it.status ==
                        "Completed"
            }
    }


    // ========================================================
    // CO2 SAVED
    // ========================================================

    fun getCO2Saved():
            Double {

        return getCurrentCustomerDevices()
            .sumOf {

                it.recoveryValue
            } * 0.35
    }
}