export function getDeviceTypeAsText(type, plural = true) {
    if (type === "AMBIENT_SENSOR") return plural ? "Ambient Sensors" : "Ambient Sensor"
    if (type === "AIR_CONDITIONER") return plural ? "Air Conditioners" : "Air Conditioner"
    if (type === "WASHING_MACHINE") return plural ? "Washing Machines" : "Washing Machine"
    if (type === "LAMP") return plural ? "Lamps" : "Lamp"
    if (type === "GATE") return plural ? "Gates" : "Gate"
    if (type === "SPRINKLER_SYSTEM") return plural ? "Sprinklers" : "Sprinkler"
    if (type === "SOLAR_PANEL_SYSTEM") return plural ? "Solar Panels" : "Solar Panel"
    if (type === "BATTERY") return plural ? "Batteries" : "Battery"
    if (type === "EV_CHARGER") return plural ? "EV Chargers" : "EV Charger"
    return type
}