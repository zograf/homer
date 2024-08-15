import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../environment";
import CircleIconButton from "../buttons/CircleIconButton";
import "./ActionsTable.css"
import {DropDownInput, DropDownSelect} from "../dropdown/DropDownInput";

export default function ActionsTable({device, token, supportedTypes}) {

    const [actions, setActions] = useState([])
    const [page, setPage] = useState(0)
    const [total, setTotal] = useState(0)

    const [start, setStart] = useState(undefined)
    const [end, setEnd] = useState(undefined)
    const [username, setUsername] = useState('')

    const [startDT, setStartDT] = useState(undefined)
    const [endDT, setEndDT] = useState(undefined)
    const [tmpUsername, setTmpUsername] = useState('')

    const [selectedType, setSelectedType] = useState("")
    const [types, setTypes] = useState([{ label: "All", value: "All" }])
    const pageSize = 10;


    useEffect(() => {
        if (supportedTypes === undefined) return
        setTypes(() => {
            const list = [{ label: "All", value: "All" }]
            supportedTypes.forEach((item) => {
                list.push({ label: item, value: item })
            })
            return list
        })
    }, [supportedTypes])
    const typeCallback = (item) => {
        setPage(0)
        if (item === "All") setSelectedType("")
        else setSelectedType(item)
    }

    const getActions = () => {
        let params = {};
        params.page = page;
        params.pageSize = pageSize;
        
        if (start !== undefined && end !== undefined) {
            params.start = start;
            params.end = end;
        }

        if (username !== '') {
            params.username = username;
        }

        params.type = selectedType
        console.log(params.type)

        params = new URLSearchParams(params);

        axios.get(API + "/device/" + device.id + "/actions", { headers: {"Authorization" : `Bearer ${token}`}, params })
            .then(response => { 
                setActions(response.data.actions)
                setPage(response.data.page)
                setTotal(response.data.totalCount)
                //console.log(response.data) 
            }).catch(e => console.log(e))
    }

    useEffect(getActions, [page, start, end, username, selectedType, device])

    const prevPage = (event) => {
        if (page > 0){
            setPage(page - 1);
        }
    }

    const nextPage = (event) => {
        if ((page + 1) * pageSize < total) {
            setPage(page + 1)
        }
    }

    const resetFilters = (event) => {
        setStart(undefined)
        setEnd(undefined)
        setUsername('')
        setStartDT(undefined)
        setEndDT(undefined)
        setTmpUsername('')
        setPage(0)
    }
    
    const setFilters = (event) => {
        setStart(startDT)
        setEnd(endDT)
        setUsername(tmpUsername)
        setPage(0)
        getActions()
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

    const handleUserFilterChange = (event) => {
        setTmpUsername(event.target['value'])
    }

    return(
        <div className="flex columns space-between v-spacer-l">
            <p className="section-title">Actions</p>
            <div className="flex center wrap gap-xs v-spacer-l">
                <div className="input-wrapper regular-border">
                    <p className="input-icon">From:</p>
                    <input type="datetime-local" value={(startDT || '').toString().substring(0, 16)} onChange={handleStartChange} />
                </div>
                <div className="input-wrapper regular-border">
                    <p className="input-icon">To:</p>
                    <input type="datetime-local" value={(endDT || '').toString().substring(0, 16)} onChange={handleEndChange} />
                </div>
                <DropDownSelect
                    placeholder={"Action Type"}
                    icon={"play_circle"}
                    options={types}
                    callback={typeCallback}/>
                <div className="input-wrapper regular-border h-spacer-xs">
                    <span className="material-symbols-outlined icon input-icon">person</span>
                    <input type="text" placeholder={"User [optional]"} value={tmpUsername} onChange={handleUserFilterChange} />
                </div>
                <div className="flex gap-xs">
                    <button className="solid-accent-button" onClick={setFilters}>filter</button>
                    <button className="outline-button" onClick={resetFilters}>reset</button>
                </div>
            </div>
            <table id="actions-table" className="v-spacer-l">
                <thead>
                    <th>Time</th>
                    <th>Triggered By</th>
                    <th>Action Type</th>
                    <th>Action Value</th>
                </thead>
                {actions?.map((item) => { // .filter((item) => item.actionType === "Open/Close")
                        return ( <ActionItem action={item} /> )
                    })}
            </table>
            <div className="flex justify-end gap-xs">
                <CircleIconButton icon={"arrow_left"} onClick={prevPage}/>
                <div className="card page-text-wrapper"> <p>PAGE {page + 1} / {Math.ceil(total / 10)}</p> </div>
                <CircleIconButton icon={"arrow_right"} onClick={nextPage}/>
            </div>
        </div>
    )
}

function ActionItem({action}) {
    const options = { year: 'numeric', month: 'short', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: false };

    return (
        <tr>
            <td>{new Date(action.dateTime).toLocaleString('en-US', options)}</td>
            <td>{action.username === "(automatic)" ? "Device" : action.username} {action.email === "(automatic)" ? "" : "(" + action.email + ")"}</td>
            <td>{action.actionType}</td>
            <td>{action.value === true ? "on" :
                 action.value === false ? "off" :
                 action.value}</td>
        </tr>
    )
}