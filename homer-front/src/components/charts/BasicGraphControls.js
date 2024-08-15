import React, {useEffect, useState} from "react";
import {DropDownSelect} from "../dropdown/DropDownInput";
import { update } from "lodash";

export default function BasicGraphControls({callback, updateFlag, supportsLive, children}){
    const [start, setStart] = useState(undefined)
    const [end, setEnd] = useState(undefined)
    const [startDT, setStartDT] = useState(undefined)
    const [endDT, setEndDT] = useState(undefined)
    const [lastXTimeUnits, setLastXTimeUnits] = useState(undefined)
    const [graphType, setGraphType] = useState("LAST_X")
    const options = [{label: "Range", value: "RANGE"}, {label: "Recent values", value: "LAST_X"}]
    console.log(supportsLive)
    if (supportsLive === undefined || supportsLive === true) options.push({label: "Live", value: "LIVE"})
    const periods = [
        {label: "Last month", value: "1M"},
        {label: "Last 3 weeks", value: "3w"},
        {label: "last 2 weeks", value: "2w"},
        {label: "Last week", value: "1w"},
        {label: "Last day", value: "1d"},
        {label: "Last 12 hours", value: "12h"},
        {label: "Last 6 hours", value: "6h"},
        {label: "Last hour", value: "1h"}
    ]

    const updateParams = () => {
        if (graphType === undefined) return;

        let params = {};
        params.live = graphType === "LIVE";
        if (graphType === "LAST_X") {
            if (lastXTimeUnits === undefined) return;
            params.lastXTimeUnits = lastXTimeUnits;
        }
        else if (graphType === "RANGE") {
            if (start === undefined || end === undefined) return;
            params.start = start;
            params.end = end;
        }

        callback(params)
    }

    useEffect(updateParams, [start, end, lastXTimeUnits, graphType])
    useEffect(() => {
        if (graphType === "LIVE") {
            updateParams()
        }
    }, [updateFlag])

    const resetFilters = (event) => {
        setStart(undefined)
        setEnd(undefined)
        setStartDT(undefined)
        setEndDT(undefined)
        setLastXTimeUnits(undefined)
    }
    const setFilters = (event) => {
        setStart(startDT)
        setEnd(endDT)
    }
    const handleStartChange = (event) => {
        if (!event.target['validity'].valid) return;
        const dt= event.target['value'];// + ':00Z';
        setStartDT(dt);
    }
    const handleEndChange = (event) => {
        if (!event.target['validity'].valid) return;
        const dt= event.target['value'];// + ':00Z';
        setEndDT(dt);
    }
    const optionsCallback = (item) => {
        if(item !== undefined){
            setGraphType(item)
        }else{
            setGraphType(undefined)
        }
    }
    const periodsCallback = (item) => {
        if (item !== undefined) setLastXTimeUnits(item)
        else setLastXTimeUnits(undefined)
    }

    return(
        <div className="flex wrap gap-xs center v-spacer-s">
            {children}
            <DropDownSelect placeholder={"Report type"} icon={"report"} options={options} callback={optionsCallback}/>
            {graphType === "RANGE" && <div className="input-wrapper regular-border">
                <p className="input-icon">From:</p>
                <input type="datetime-local" value={(startDT || '').toString().substring(0, 16)}
                       onChange={handleStartChange}/>
            </div>}
            {graphType === "RANGE" && <div className="input-wrapper regular-border">
                <p className="input-icon">To:</p>
                <input type="datetime-local" value={(endDT || '').toString().substring(0, 16)} onChange={handleEndChange} />
            </div>}
            {graphType === "LAST_X" && <DropDownSelect placeholder={"Period"} icon={""} options={periods} callback={periodsCallback} />}
            <div className="flex gap-xs">
                {graphType === "RANGE" && <button className="solid-accent-button" onClick={setFilters}>filter</button>}
                {graphType === "RANGE" && <button className="outline-button" onClick={resetFilters}>reset</button>}
            </div>
        </div>
    )
}

export function NoGraphData() {
    return(
        <div className="w-100 flex center justify-center columns dashed-card v-spacer-l">
            <p className="tutorial-text">No Data</p>
            <p className="card-label">Select some filters to see your data</p>
        </div>
    )
}
