import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../environment";
import Chart from "react-apexcharts";
import BasicGraphControls, { NoGraphData } from "../../components/charts/BasicGraphControls";
import { useParams } from "react-router-dom";

export default function OccupiedSlotsChart({device, token}) {
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

        params = new URLSearchParams(params);
        let path = API + "/device/" + device.id + "/slots"

        axios.get(path, { headers: {"Authorization" : `Bearer ${token}`}, params })
            .then(response => {
                const points = []
                response.data.forEach((item) => {
                    points.push({
                        x: new Date(item.dateTime).getTime(),
                        y: item.value
                    })
                })
                setSeries([{
                    data: points
                }])
            })
            .catch(e => console.log(e))
    }

    // Web socket
    const [socket, setSocket] = useState(null)
    const [flag, setFlag] = useState(0)
    const params = useParams()
    const shared = params.shared

    useEffect(() => {
        let id = device.propertyId
        let deviceId = device.id
        let path = ""
        if (shared)
            path =  "ws://localhost:8080/ws?deviceId=" + deviceId
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
            }
        });
    }

    return(
        <main className="mh-100">
            <h1 className="section-title">Occupied Slots</h1>
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
