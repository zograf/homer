import React, {useState} from "react";
import BasicGraphControls, {NoGraphData} from "./BasicGraphControls";
import Chart from "react-apexcharts";
import axios from "axios";
import {API} from "../../environment";

export default function PlatePresenceChart({device, token}) {

    const [plate, setPlate] = useState('')

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
                        return val === 1.0 ? "Parked" : "Not Parked"
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

    const handlePlate = (event) => {
        setPlate(event.target['value'])
    }
    const handleRequest = (params) => {
        if(plate === "") return
        params.plate = plate

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
        let path = API + "/device/" + device.id + "/data/plate"


        axios.get(path, { headers: {"Authorization" : `Bearer ${token}`}, params })
            .then(response => {
                const points = []
                response.data.forEach((item) => {
                    points.push({
                        x: new Date(item.dateTime).getTime(),
                        y: item.value === 1.0 ? 0.0 : 1.0
                    })
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
                setData((prevData) => {
                    return {
                        options: { ...prevData.options },
                        series : [{
                            name: "Presence",
                            data: points
                        }]
                    }
                })
                console.log(response.data)
            })
            .catch(e => console.log(e))
    }

    return(
        <main className="mh-100">
            <p className="section-title">Car Presence Graph</p>
            <BasicGraphControls callback={handleRequest} supportsLive={false}>
                <div className="input-wrapper regular-border">
                    <span className="material-symbols-outlined icon input-icon">123</span>
                    <input type="text" placeholder={"Plate"} value={plate} onChange={handlePlate} />
                </div>
            </BasicGraphControls>
            {
                data.series[0].data.length > 0 ?
                    <Chart
                        options={data.options}
                        series={data.series}
                        type="line"
                        height={300}

                    /> : <NoGraphData/>
            }
        </main>
    )
}