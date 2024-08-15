import CircleIconButton from "../../../components/buttons/CircleIconButton";
import {DeviceMetaData, ModuleWrapper} from "../components/DeviceCard";
import "./ViewDevicePage.css"
import StatusModule from "../../../components/modules/StatusModule";
import PowerProductionModule from "../../../components/modules/PowerProductionModule";
import SolarPanelSystemModule from "../../../components/modules/SolarPanelSystemModule";
import BatteryModule from "../../../components/modules/BatteryModule";
import EvChargerModule from "../../../components/modules/EvChargerModule";
import AmbientLightModule from "../../../components/modules/AmbientLightModule";
import AmbientSensorModule from "../../../components/modules/AmbientSensorModule";
import AirConditionModule from "../../../components/modules/AirConditionModule";
import WashingMachineModule from "../../../components/modules/WashingMachineModule";
import SmartGateModule from "../../../components/modules/SmartGateModule";
import EditPlates from "../../../components/EditPlates";
import ActionsTable from "../../../components/tables/ActionsTable";
import SchedulingModule from "../../../components/modules/SchedulingModule";
import AirConditionDetailsModule from "../../../components/modules/AirConditionDetailsModule";
import WashingMachineSchedulingModule from "../../../components/modules/WashingMachineSchedulingModule";
import LightPresenceChart from "../../../components/charts/LightPresenceChart";
import AmbientSensorChart from "../../../components/charts/AmbientSensorChart";
import ConsumptionGraph from "../../properties/components/ConsumptionGraph";
import SlotsModule from "../../../components/modules/SlotsModule";
import SprinklerScheduleModule from "../../../components/modules/SprinklerScheduleModule";
import StatusGraph from "../../../components/charts/StatusGraph";
import OccupiedSlotsChart from "../../../components/charts/OccupiedSlotsChart";
import ChargingProgressChart from "../../../components/charts/ChargingProgressChart";
import ShareModule from "../../../components/modules/ShareModule";
import PlatePresenceChart from "../../../components/charts/PlatePresenceChart";

export default function ViewDevicePage({device, goBack, shared}) {
    const token = localStorage.getItem("token")
    const supportedTypes = {
        "GATE" : ["Open/Close", "Presence", "Public/Private"],
        "LAMP" : ["Turn on/off", "Auto Brightness"],
        "EV_CHARGER" : ["Stop charging", "Start charging", "Fill to percent"],
        "SOLAR_PANEL_SYSTEM" : ["Turn on/off"],
        "AIR_CONDITIONER": ["Turn on/off", "Temperature changed", "Mode changed", "Schedule start", "Schedule end"],
        "WASHING_MACHINE" : ["Turn on/off", "Schedule start", "Schedule end", "Mode changed"],
        "SPRINKLER_SYSTEM" : ["Turn on/off"]
    }

    return(
        <main className="w-100">
            <div className="flex gap-s v-spacer-l">
                <CircleIconButton label={"Back"} icon={"chevron_left"} onClick={goBack}/>
                <p className="section-title">{device?.name}</p>
            </div>
            <div className="flex gap-xs w-100 v-spacer-l">
                <div className="card default-card">
                    <DeviceMetaData device={device}/>
                </div>
                {/* TODO Replace some of the Status Modules with detailed status modules if necessary */}
                {device.modules.sort((a, b) => a.type.localeCompare(b.type)).map((module) => {
                    if(module.type === "STATUS") return (<div className="card flex"> <StatusModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "POWER_PRODUCTION") return (<div className="card flex"> <PowerProductionModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "SOLAR_PANEL_SYSTEM") return (<div className="card flex"> <SolarPanelSystemModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "BATTERY") return (<div className="card flex"> <BatteryModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "EV_CHARGER") return (<div className="card flex"> <EvChargerModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "AMBIENT_LIGHT") return (<div className="card flex"> <AmbientLightModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "AMBIENT_SENSOR") return (<div className="card flex"> <AmbientSensorModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "AIR_CONDITIONER") return (
                    <>
                        <div className="card flex"> 
                            <AirConditionDetailsModule module={module} device={device} token={token}/>
                        </div>
                        <div className="card flex"> 
                            <AirConditionModule module={module} device={device} token={token}/> 
                        </div>
                    </>)
                    if(module.type === "SCHEDULING") return (<></>)
                    if(module.type === "SMART_GATE") return (<div className="card flex"> <SmartGateModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "WASHING_MACHINE") return (<div className="card flex"> <WashingMachineModule module={module} device={device} token={token}/> </div>)
                    if(module.type === "SLOTS") return (<></>)
                    if(module.type === "SPRINKLER_SCHEDULE") return (<></>)
                    // TODO Add other modules
                    // else return( <p>Not Implemented</p> )
                }) }
                {
                    shared == "true" ? <></> : <div className = "card flex"> <ShareModule device={device} token={token}/></div>
                }
            </div>
            {device.type === "EV_CHARGER" && <SlotsModule module={device.modules.filter((module) => module.type === "SLOTS")[0]} device={device} token={token}/>}
            {device.type === "AIR_CONDITIONER" && <SchedulingModule deviceId={device.id} schedulingModule={device.modules.filter((module) => module.type === "SCHEDULING")[0]} acModule={device.modules.filter((module) => module.type === "AIR_CONDITIONER")[0]}/>}
            {device.type === "WASHING_MACHINE" && <WashingMachineSchedulingModule deviceId={device.id} schedulingModule={device.modules.filter((module) => module.type === "SCHEDULING")[0]} acModule={device.modules.filter((module) => module.type === "AIR_CONDITIONER")[0]}/>}
            {device.type === "GATE" && <EditPlates deviceId={device.id} plates={device.modules.filter((module) => module.type === "SMART_GATE")[0].plates} />}
            {device.type === "SPRINKLER_SYSTEM" && <SprinklerScheduleModule deviceId={device.id} token={token} schedule={device.modules.filter((module) => module.type === "SPRINKLER_SCHEDULE")[0]}/>}

            {["SOLAR_PANEL_SYSTEM", "EV_CHARGER", "LAMP", "GATE", "AIR_CONDITIONER", "WASHING_MACHINE", "SPRINKLER_SYSTEM"].includes(device.type) && <ActionsTable device={device} supportedTypes={supportedTypes[device.type]} token={token}/>}
            {device.type === "GATE" && <PlatePresenceChart device={device} token={token}/>}
            {device.type === "LAMP" && <LightPresenceChart deviceId={device.id} token={token} device={device}/> }
            {device.type === "AMBIENT_SENSOR" && <AmbientSensorChart deviceId={device.id} token={token} device={device}/> }
            {device.type === "EV_CHARGER" && <ChargingProgressChart device={device} token={token} module={device.modules.filter((module) => module.type === "EV_CHARGER")[0]}/>}
            {device.type === "EV_CHARGER" && <OccupiedSlotsChart device={device} token={token}/>}
            
            <ConsumptionGraph propertyId={undefined} cityId={undefined} device={device} token={token}/>
            <StatusGraph deviceId={device.id} token={token}/>
        </main>
    )
}