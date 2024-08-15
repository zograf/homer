import {DropDownSelect} from "../dropdown/DropDownInput";
import BasicGraphControls, { NoGraphData } from "./BasicGraphControls";
import Chart from "react-apexcharts";
import React, {useState} from "react";
import {API} from "../../environment";
import axios from "axios";

export default function StatusGraph({deviceId, token}) {
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
                        return val == 1.0 ? "Online" : "Offline"
                    }
                },
                tickAmount: 1,
            },
            colors: ['#c88214']
        },
        series: [{
            data: []
        }],

    })
    const [onlinePercent, setOnlinePercent] = useState(0)

    const timeLengths = new Map([
        ['1M', 2629800000],
        ['3w', 1814400049],
        ['2w', 1209600033],
        ['1w', 604800016],
        ['1d', 86400000],
        ['12h', 43200000],
        ['6h', 21600000],
        ['1h', 3600000]
    ]);

    const handleRequest = (params) => {
        let isLive = params.live
        let type = ""
        let start = undefined
        let end = undefined

        if (params.lastXTimeUnits !== undefined) {
            type = "LAST_X"
            end = new Date().getTime()
            start = end - timeLengths.get(params.lastXTimeUnits)
        }
        if (params.start !== undefined && params.end !== undefined) {
            type = "RANGE"
            start = new Date(params.start).getTime()
            end = new Date(params.end).getTime()
        }
        params = new URLSearchParams(params);
        let path = API + "/device/" + deviceId + "/status"

        axios.get(path, { headers: {"Authorization" : `Bearer ${token}`}, params })
            .then(response => {

                const points = []
                let skipFirst = true
                if(response.data.length >= 1) {
                    response.data.forEach((item) => {
                        if(!skipFirst) points.push({
                            x: new Date(item.dateTime).getTime(),
                            y: item.value === 1.0 ? 0.0 : 1.0
                        })
                        else skipFirst = false
                        points.push({
                            x: new Date(item.dateTime).getTime(),
                            y: item.value
                        })
                    })
                    if (type === "RANGE") {
                        points.unshift({x: start, y: points[0].y})
                        points.push({x: end, y: points[points.length - 1].y})
                    } else if (type === "LAST_X") {
                        points.unshift({x: start, y: points[0].y})
                        points.push({x: end, y: points[points.length - 1].y})
                    }
                    let offlinePoints = points.filter(point => point.y < 1)
                    let totalTimeOffline = 0
                    for (let i = 0; i < offlinePoints.length / 2; i += 2) totalTimeOffline += offlinePoints[i + 1].x - offlinePoints[i].x
                    setOnlinePercent(((1 - totalTimeOffline / (end - start)) * 100).toFixed(2))
                }
                else {
                    points.push({x: start, y: 1.0})
                    points.push({x: end, y: 1.0})
                    setOnlinePercent(100)
                }
                setData((prevData) => {
                    if (isLive) {
                        return {
                            options: {...prevData.options},
                            series : [{
                                name: "Status",
                                data: points.slice(SLICE_SIZE)
                            }]
                        }
                    }
                    return {
                        options: {...prevData.options},
                        series : [{
                            name: "Status",
                            data: points
                        }]
                    }
                })
                console.log("Percentage:", onlinePercent)
            })
            .catch(e => console.log(e))
    }

    return(
        <main className="mh-100">
            <p className="section-title">Status Graph</p>
            <BasicGraphControls callback={handleRequest} supportsLive={false}/>
            {
                data.series[0].data.length > 0 ?
                <div>
                    <Chart
                        options={data.options}
                        series={data.series}
                        type="line"
                        height={300}

                    />
                    <HorizontalBar percentage={onlinePercent}/>
                </div> : <NoGraphData/>
            }
        </main>
    )
}

function HorizontalBar({percentage}) {
    return(
        <div style={{height:"32px", width:"100%", borderRadius:"8px", backgroundColor:"rgb(var(--neutral))"}}>
            <div className="card-accent flex center space-between" style={{height:"100%", width: percentage + "%", borderRadius:"8px", backgroundColor:"white", paddingLeft:"8px"}}>
                <p className="card-body">Online Percent</p>
                <p className="card-title h-spacer-xs">{percentage}%</p>
            </div>
        </div>
    )
}