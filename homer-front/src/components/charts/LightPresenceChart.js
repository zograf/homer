import Chart from "react-apexcharts";
import React, {useEffect, useState} from "react";
import {API} from "../../environment";
import axios from "axios";
import {DropDownSelect} from "../dropdown/DropDownInput";
import BasicGraphControls, { NoGraphData } from "./BasicGraphControls";

export default function LightPresenceChart({token, deviceId, device}) {
    const [live, setLive] = useState(false)
    const [SLICE_SIZE, _] = useState(-10)

    const [data, setData] = useState({
        options: {
            xaxis: {
                labels: {
                    enabled: true,
                    formatter: function (val) {
                        const options = { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: false };
                        return new Date(val).toLocaleString('en-US', options)
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
        },
        series: [{
            data: []
        }],

    })

    useEffect(() => {
        if (live) {
            setData((prevData) => {
                const module = device.modules.filter((item) => item.type === "AMBIENT_LIGHT")[0]
                const newData = {
                    options: {...prevData.options},
                    series: [{
                        name: prevData.series[0].name,
                        data: [...(prevData.series[0].data.slice(SLICE_SIZE)), {
                            x: new Date().getTime(),
                            y: module.lightPresence
                        }]
                    }]
                }
                return newData
            })
        }
    }, [device])

    const handleRequest = (params) => {
        setLive(params.live)
        let isLive = params.live

        params = new URLSearchParams(params);
        let path = API + "/device/" + deviceId + "/light"

        axios.get(path, { headers: {"Authorization" : `Bearer ${token}`}, params })
            .then(response => {
                const points = []
                response.data.forEach((item) => {
                    points.push({
                        x: new Date(item.dateTime).getTime(),
                        y: item.value
                    })
                })
                setData((prevData) => {
                    if (isLive) {
                        return {
                            options: { ...prevData.options },
                            series : [{
                                name: "Light presence",
                                data: points.slice(SLICE_SIZE)
                            }]
                        }
                    }
                    return {
                        options: { ...prevData.options },
                        series : [{
                            name: "Light presence",
                            data: points
                        }]
                    }
                })
                console.log(response.data)
            })
            .catch(e => console.log(e))
    }

    return (
        <main>
            <p className="section-title">Light Presence Graph</p>
            <BasicGraphControls callback={handleRequest}/>
            {
                data.series[0].data.length > 0 ?
                <Chart
                    options={data.options}
                    series={data.series}
                    type="line"
                    height={300}

                /> :
                <NoGraphData/>
            }
        </main>
    );
}
