import Chart from "react-apexcharts";
import React, {useEffect, useState} from "react";
import {API} from "../../environment";
import axios from "axios";
import {DropDownSelect} from "../dropdown/DropDownInput";
import BasicGraphControls, { NoGraphData } from "./BasicGraphControls";

export default function AmbientSensorChart({token, deviceId, device}) {
    const [live, setLive] = useState(false)

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
            colors: ["#FF0000", '#c88214']
        },
        series: [{
            data: []
        }],

    })

    const [SLICE_SIZE, _] = useState(-10)

    useEffect(() => {
        if (live) {
            setData((prevData) => {
                const newData = {
                    options: {...prevData.options},
                    series: [{
                        name: prevData.series[0].name,
                        data: [...(prevData.series[0].data.slice(SLICE_SIZE)), {
                            x: new Date().getTime(),
                            y: device.modules[0].temperatureValue
                        }]
                    }, 
                    {
                        name: prevData.series[1].name,
                        data: [...(prevData.series[1].data.slice(SLICE_SIZE)), {
                            x: new Date().getTime(),
                            y: device.modules[0].humidityPercent
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
        let path = API + "/device/" + deviceId + "/ambientTemperature"

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
                    return {
                        options: {...prevData.options},
                        series : [{
                            name: "Temperature",
                            data: points
                        }]
                    }
                })
                console.log(response.data)

                let path = API + "/device/" + deviceId + "/ambientHumidity"
                axios.get(path, { headers: {"Authorization" : `Bearer ${token}`}, params })
                    .then(response => {
                        const points = []
                        response.data.forEach((item) => {
                            points.push({
                                x: new Date(item.dateTime).getTime(),
                                y: item.value
                            })
                        })
                        setData(prevData => {
                            if (isLive) {
                                console.log("WOOHOO LIVE")
                                return {
                                    options: { ...prevData.options },
                                    series : [
                                        {
                                            name: prevData.series[0].name,
                                            data: prevData.series[0].data.slice(SLICE_SIZE)
                                        },
                                        {
                                            name: "Humidity",
                                            data: points.slice(SLICE_SIZE)
                                        }],
                                    yaxis: [
                                        {
                                            seriesName: prevData.series[0].name
                                        },
                                        {
                                            seriesName: "Humidity"
                                        }
                                    ]
                                }
                            }
                            return {
                                options: { ...prevData.options },
                                series : [
                                    {
                                        name: prevData.series[0].name,
                                        data: prevData.series[0].data
                                    },
                                    {
                                        name: "Humidity",
                                        data: points
                                    }],
                                yaxis: [
                                    {
                                        seriesName: prevData.series[0].name
                                    },
                                    {
                                        seriesName: "Humidity"
                                    }
                                ]
                            }
                        })
                        console.log(response.data)
                    })
                    .catch(e => console.log(e))
            })
            .catch(e => console.log(e))
    }
    console.log(data);

    return (
        <main>
            <p className="section-title">Temperature & Humidity Graph</p>
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
