import {useEffect, useState, useReducer} from "react";
import axios from "axios";
import {API} from "../../environment";
import {UserPage} from "../root/UserPage";
import {useParams} from 'react-router-dom';
import { DeviceCard, NoDevice } from "./components/DeviceCard";
import {getDeviceTypeAsText} from "../../utils/DeviceUtils";
import _ from 'lodash';

export default function DevicesPage() {

    const params = useParams()
    const propertyId = params.propertyId;
    const userId = localStorage.getItem("id")
    const token = localStorage.getItem("token")
    const [requests, setRequests] = useState([])
    const [socket, setSocket] = useState([])
    const shared = params.shared
    console.log(propertyId != 0)


    useEffect(() => {
        if (propertyId != 0) {
            axios.get(API + "/device/property/" + propertyId, { headers: {"Authorization" : `Bearer ${token}`} })
                .then(response => { 
                    setRequests(response.data)
                    const socket = new WebSocket("ws://localhost:8080/ws?propertyId="+propertyId)
                    setSocket([socket])
                })
                .catch(e => console.log(e))
        } else {
            axios.get(API + "/user/" + userId + "/getPermissions", { headers: {"Authorization" : `Bearer ${token}`} })
                .then(response => { 
                    setRequests(response.data.devices)
                    let sockets = []
                    for (let i = 0; i < response.data.devices.length; i++) {
                        let device = response.data.devices[i]
                        const socket = new WebSocket("ws://localhost:8080/ws?deviceId="+device.id)
                        sockets.push(socket)
                    }
                    setSocket(sockets)
                    console.log(response.data) 
                })
                .catch(e => console.log(e))
        }
    }, [])

    if (socket.length != 0) {
        for (let i = 0; i < socket.length; i++) {
            socket[i].addEventListener("open", event => {
                socket[i].send("Connection established")
            });

            socket[i].addEventListener("message", event => {
                let newData = JSON.parse(event.data)

                if (requests.filter(e => e.id === newData.id).length > 0) {
                    setRequests((prevList) => {
                        const updatedList = prevList.map((device) =>
                            device.id === newData.id ? newData : device
                        );
                        return updatedList;
                    });
                } else {
                    setRequests(prevList => [...prevList, newData])
                }
            });
        }
    }

    return(
        <UserPage>
            <main className="mh-100">
                <h1 className="page-title">Welcome Back!</h1>
                <div className="flex wrap gap-xs">
                    { requests.length > 0 ? <AllDevices devices={requests} shared={shared}/> : <NoDevice/> }
                </div>
            </main>
        </UserPage>
    )
}

function groupDevicesByType(devices) {
    return devices.reduce((result, device) => {
        const { type } = device;
        if (!result[type]) {
            result[type] = [];
        }
        result[type].push(device);
        return result;
    }, {});
}

function AllDevices({devices, shared}) {
    const groupedDevices = groupDevicesByType(devices)
    const token = localStorage.getItem("token")

    return(
        <div className="w-100">
            {Object.entries(groupedDevices).sort(([typeA], [typeB]) => typeA.localeCompare(typeB)).map(([type, devicesOfType]) => (
                <div key={type}>
                    <p className="section-title">{getDeviceTypeAsText(type)}</p>
                    <div className="flex gap-xs wrap v-spacer-l">
                        {devicesOfType.map((item) => (<DeviceCard device={item} token={token} shared={shared}/>))}
                    </div>
                </div>
            ))}
        </div>
    )
}
