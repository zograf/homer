import './DeviceCard.css'
import {useEffect, useState} from "react";
import axios from "axios";
import {IMG} from "../../../environment";
import {API} from "../../../environment";
import {capitalizeString} from "../../../utils/Transforamtions";
import SolarPanelSystemModule from "../../../components/modules/SolarPanelSystemModule";
import StatusModule from "../../../components/modules/StatusModule";
import PowerProductionModule from "../../../components/modules/PowerProductionModule";
import BatteryModule from "../../../components/modules/BatteryModule";
import EvChargerModule from "../../../components/modules/EvChargerModule";
import AmbientLightModule from "../../../components/modules/AmbientLightModule";
import AmbientSensorModule from "../../../components/modules/AmbientSensorModule";
import AirConditionModule from "../../../components/modules/AirConditionModule";
import AirConditionDetailsModule from "../../../components/modules/AirConditionDetailsModule";
import {PopUpPage} from "../../../components/popup/PopUpFrame";
import ViewDevicePage from "../view/ViewDevicePage";
import SmartGateModule from "../../../components/modules/SmartGateModule";
import WashingMachineModule from '../../../components/modules/WashingMachineModule';
import SlotsModule from '../../../components/modules/SlotsModule';

export function DeviceCard({token, device, shared}) {

    const [viewDevice, setViewDevice] = useState(false)

    return(
        <main>
            <div className="card device-wrapper clickable showing" onClick={() => { setViewDevice(true)}}>
                <DeviceMetaData device={device}/>
                {device.modules.sort((a, b) => a.type.localeCompare(b.type)).map((module) => {
                    if(module.type === "STATUS") return (<ModuleWrapper> <StatusModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "POWER_PRODUCTION") return (<ModuleWrapper> <PowerProductionModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "SOLAR_PANEL_SYSTEM") return (<ModuleWrapper> <SolarPanelSystemModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "BATTERY") return (<ModuleWrapper> <BatteryModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "EV_CHARGER") return (<ModuleWrapper> <EvChargerModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "AMBIENT_LIGHT") return (<ModuleWrapper> <AmbientLightModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "AMBIENT_SENSOR") return (<ModuleWrapper> <AmbientSensorModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "AIR_CONDITIONER") return (<ModuleWrapper> <AirConditionDetailsModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "SMART_GATE") return (<ModuleWrapper> <SmartGateModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "WASHING_MACHINE") return (<ModuleWrapper> <WashingMachineModule module={module} device={device} token={token}/> </ModuleWrapper>)
                    if(module.type === "SCHEDULING") return (<></>)
                    if(module.type === "SLOTS") return (<></>)
                    else return( <></>)
                }) }
            </div>

            <PopUpPage visible={viewDevice}>
                <ViewDevicePage device={device} goBack={() => setViewDevice(false)} shared={shared}/>
            </PopUpPage>
        </main>
    )
}

export function DeviceMetaData({device}) {
    const token = localStorage.getItem("token")
    const [image, setImage] = useState(null)
    useEffect(() => {
        axios.get(IMG + `/device-${device.id}`, { headers: { "Authorization": `Bearer ${token}`}, responseType: "blob" })
            .then(response => {
                setImage(response.data)
            })
            .catch(e => console.log(e))
    }, []);
    return(
        <main className="flex">
            <div className="device-img-wrapper device-img h-spacer-s flex justify-center">
                <img
                    className="device-img"
                    alt="not found"
                    src={image == null ? require('../../../img/default.png') : URL.createObjectURL(image)}
                />
            </div>
            <div className="flex space-between columns">
                <div>
                    <p className="card-title">{device.name}</p>
                    <p className="card-label">{device.online === true ? 'Online' : 'Offline'}</p>
                </div>
                { (device.type !== "SOLAR_PANEL_SYSTEM" && device.type !== "BATTERY") && <div className="data-grid">

                    <p className="key-item">Power Supply:</p>
                    <p className="value-item">{capitalizeString(device.powerSupply)}</p>

                    <p className="key-item">Consumption:</p>
                    <p className="value-item">{device.consumption} kW</p>

                </div>}
            </div>
        </main>
    )
}

export function ModuleWrapper(props) {
    return(
        <div className="flex">
            <div className="vertical-line"></div>
            {props.children}
        </div>
    )
}

export function NoDevice() {
    return(
        <div className="dashed-container flex center justify-center columns gap-s showing">
            <p className="tutorial-text">You don't have devices.</p>
            <button className="solid-button" onClick={() => { window.location.href = '/device/add'}}>Add Device</button>
        </div>
    )
}
