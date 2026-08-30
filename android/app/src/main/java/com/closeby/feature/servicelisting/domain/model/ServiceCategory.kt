package com.closeby.feature.servicelisting.domain.model

/**
 * Top level service categories offered on Close by.
 * Owned by Agent 3 (Service Listing + Search + Filter module).
 */
enum class ServiceCategory(val displayName: String, val emoji: String) {
    VEHICLES("Vehicles", "🚗"),
    LABOUR("Labour", "👷"),
    EQUIPMENT("Equipment", "🛠️");

    fun subcategories(): List<ServiceSubcategory> =
        ServiceSubcategory.entries.filter { it.parent == this }
}

/**
 * Subcategories per top level category, as defined in the product spec.
 * Each subcategory tracks its parent [ServiceCategory] so the UI can
 * derive the list without a separate mapping table.
 */
enum class ServiceSubcategory(val parent: ServiceCategory, val displayName: String) {
    // Vehicles
    BICYCLE(ServiceCategory.VEHICLES, "Bicycle"),
    MOTORCYCLE(ServiceCategory.VEHICLES, "Motorcycle"),
    SCOOTER(ServiceCategory.VEHICLES, "Scooter"),
    AUTO(ServiceCategory.VEHICLES, "Auto"),
    E_RICKSHAW(ServiceCategory.VEHICLES, "E-rickshaw"),
    CAR(ServiceCategory.VEHICLES, "Car"),
    PICKUP(ServiceCategory.VEHICLES, "Pickup"),
    VAN(ServiceCategory.VEHICLES, "Van"),
    TRACTOR(ServiceCategory.VEHICLES, "Tractor"),
    TRUCK(ServiceCategory.VEHICLES, "Truck"),
    BUS(ServiceCategory.VEHICLES, "Bus"),
    VEHICLE_OTHER(ServiceCategory.VEHICLES, "Other"),

    // Labour
    CONSTRUCTION_LABOUR(ServiceCategory.LABOUR, "Construction Labour"),
    MASON(ServiceCategory.LABOUR, "Mason"),
    ELECTRICIAN(ServiceCategory.LABOUR, "Electrician"),
    PLUMBER(ServiceCategory.LABOUR, "Plumber"),
    PAINTER(ServiceCategory.LABOUR, "Painter"),
    CARPENTER(ServiceCategory.LABOUR, "Carpenter"),
    DRIVER(ServiceCategory.LABOUR, "Driver"),
    AGRICULTURAL_WORKER(ServiceCategory.LABOUR, "Agricultural Worker"),
    WELDER(ServiceCategory.LABOUR, "Welder"),
    MECHANIC(ServiceCategory.LABOUR, "Mechanic"),
    LABOUR_OTHER(ServiceCategory.LABOUR, "Other"),

    // Equipment
    DRILL_MACHINE(ServiceCategory.EQUIPMENT, "Drill Machine"),
    WATER_PUMP(ServiceCategory.EQUIPMENT, "Water Pump"),
    GENERATOR(ServiceCategory.EQUIPMENT, "Generator"),
    SPRAYER(ServiceCategory.EQUIPMENT, "Sprayer"),
    WELDING_MACHINE(ServiceCategory.EQUIPMENT, "Welding Machine"),
    RICE_PADDY_MACHINE(ServiceCategory.EQUIPMENT, "Rice/Paddy Machine"),
    CONCRETE_MIXER(ServiceCategory.EQUIPMENT, "Concrete Mixer"),
    AGRICULTURAL_MACHINERY(ServiceCategory.EQUIPMENT, "Agricultural Machinery"),
    CONSTRUCTION_EQUIPMENT(ServiceCategory.EQUIPMENT, "Construction Equipment"),
    EQUIPMENT_OTHER(ServiceCategory.EQUIPMENT, "Other");
}
