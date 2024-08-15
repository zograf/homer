import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../../environment";
import {UserPage} from "../../root/UserPage";
import {useParams} from 'react-router-dom';
import {DropDownInput, DropDownSelect} from "../../../components/dropdown/DropDownInput";
import Chart from "react-apexcharts";
import BasicGraphControls, { NoGraphData } from "../../../components/charts/BasicGraphControls";

export default function ConsumptionGraph({propertyId, cityId, device, token}) {
    const [options, setOptions] = useState({
        xaxis: {
            labels: {
                enabled: true,
                formatter: function (val) {
                    const opt = { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: false };
                    return new Date(val).toLocaleString('en-US', opt)
                }
            },
        },
        yaxis: {
            labels: {
                formatter: function (val) {
                    return val.toFixed(3)
                }
            }
        },
        colors: ['#c88214']
    })
    const [series, setSeries] = useState([{
        data: []
    }])
    
    const handleRequest = (params) => {

        if (type === undefined && device === undefined) return;

        if (cityId !== undefined) {
            params.cityId = cityId;
        }

        let payload = new FormData()
        for (const [key, value] of Object.entries(params)) {
            payload.append(key, value)
        }

        let path = API + "/property/" + propertyId + "/" + type //"consumption"
        if (cityId !== undefined) {
            path = API + "/property/" + type
        }
        if (device !== undefined && device.type !== "BATTERY") {
            path = API + "/device/" + device.id + "/consumption"
        }
        if (device !== undefined && device.type === "BATTERY") {
            path = API + "/device/" + device.id + "/battery"
        }

        axios.post(path, payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                const points = []
                response.data.forEach((item) => {
                    if (device !== undefined && device.type === "SOLAR_PANEL_SYSTEM"){
                        points.push({
                            x: new Date(item.dateTime).getTime(),
                            y: -item.value
                        })
                    }else{
                        points.push({
                            x: new Date(item.dateTime).getTime(),
                            y: item.value
                        })
                    }
                })
                setSeries([{
                    data: points
                }])
                //console.log(response.data)
            })
            .catch(e => console.log(e))
    }

    const [type, setType] = useState(undefined)
    const consumptionOptions = [{label: "Total", value: "consumption"}, {label: "Electricity distribution delta", value: "delta"}]
    
    const optionsCallback = (item) => {
        if(item !== undefined){
            setType(item)
        }else{
            setType(undefined)
        }
    }

    // Web socket
    const [socket, setSocket] = useState(null)
    const [flag, setFlag] = useState(0)
    const params = useParams()
    const shared = params.shared

    useEffect(() => {
        if (cityId !== undefined) return;
        let id = propertyId === undefined ? device.propertyId : propertyId
        let path = ""
        if (shared)
            path =  "ws://localhost:8080/ws?deviceId=" + device.id
        else
            path =  "ws://localhost:8080/ws?propertyId=" + id
        const socket = new WebSocket(path)
        setSocket(socket)
    }, [])    

    if (socket != null) {
        socket.addEventListener("open", event => {
            socket.send("Connection established")
        });

        socket.addEventListener("message", event => {
            let newData = JSON.parse(event.data)

            if (device !== undefined) {
                if (newData.id === device.id) {
                    setFlag((flag + 1) % 1000)
                }
            } else if (propertyId !== undefined) {
                setFlag((flag + 1) % 1000)
            }
        });
    }

    return(
        <main className="mh-100">
            { (device === undefined && cityId === undefined) && <h1 className="section-title">Property Power Consumption</h1>}
            { (device === undefined && propertyId === undefined) && <h1 className="section-title">City Power Consumption</h1>}
            { (device !== undefined && device.type !== "SOLAR_PANEL_SYSTEM" && device.type !== "BATTERY") && <h1 className="section-title">Power Consumption</h1>}
            { (device !== undefined && device.type === "SOLAR_PANEL_SYSTEM") && <h1 className="section-title">Power Production</h1>}
            { (device !== undefined && device.type === "BATTERY") && <h1 className="section-title">Continual Graph</h1>}
            { device === undefined && <div className="v-spacer-xs"><DropDownSelect placeholder={"Consumption type*"} icon={"report"} options={consumptionOptions} callback={optionsCallback} /></div>}
            <BasicGraphControls updateFlag={flag} callback={handleRequest}/>
            {
                series[0].data.length > 0 ?
                <Chart
                    options={options}
                    series={series}
                    type="line"
                    height={300}

                /> :
                <NoGraphData/>
            }
        </main>
    )
}
