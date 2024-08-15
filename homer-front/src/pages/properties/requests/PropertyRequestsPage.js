import {NoPropertyRequests, PropertyRequest} from '../components/PropertyCard'
import './PropertyRequestsPage.css'
import { AdminPage } from '../../root/AdminPage'
import React, {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../../environment";
import { StandardPopUp, usePopup} from "../../../components/popup/PopUpFrame";

export default function PropertyRequestsPage() {
    const token = localStorage.getItem("token")
    const [requests, setRequests] = useState([])
    const fetch = () => {
        axios.get(API + "/property/requests", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setRequests(response.data) })
            .catch(e => console.log(e))
    }
    useEffect(() => fetch(), [])

    const approvePopup = usePopup()
    const handleApprove = (id) => {
        axios.get(API + `/property/request/approve/${id}`, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { fetch() })
            .catch(e => console.log(e))
    }

    const denyPopup = usePopup()
    const handleDeny = (id, message) => {
        let payload = { "id": id, "reason": message }
        axios.post(API + "/property/request/deny", payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { fetch() })
            .catch(e => console.log(e))
    }

    return(
        <AdminPage>
            <main className="mh-100">

                <h1 className="page-title">Manage Property Requests</h1>
                <div className="flex gap-xs wrap">
                    { requests.length > 0 ? requests?.map((item) => {
                        return ( <PropertyRequest token={token} property={item} approveCallback={approvePopup.showPopup} denyCallback={denyPopup.showPopup}/> )
                    }) : <NoPropertyRequests/> }
                </div>
                <ApprovePopUp popup={approvePopup} approveCallback={handleApprove}/>
                <DenyPopUp popup={denyPopup} denyCallback={handleDeny}/>
            </main>
        </AdminPage>
    )
}

function ApprovePopUp({popup, approveCallback}) {
    return(
        <StandardPopUp
            visible={popup.isVisible}
            title={"Approve confirmation"}
            description={"Are you sure that this property is valid and can be used in the system?"}
            neutralLabel={"Cancel"}
            positiveLabel={"Approve"}
            positiveCallback={popup.onPositiveCallback(approveCallback)}
            neutralCallback={popup.onNeutralCallback()}
        />
    )
}

function DenyPopUp({popup, denyCallback}) {

    const [msg, setMsg] = useState("")
    const handleDenyPopUp = (id) => {
        denyCallback(id, msg)
        setMsg("")
    }

    return(
        <StandardPopUp
            visible={popup.isVisible}
            title={"Deny Property"}
            description={"Please enter the reason for denying the property"}
            neutralLabel={"Cancel"}
            positiveLabel={"Deny"}
            positiveCallback={popup.onPositiveCallback(handleDenyPopUp)}
            neutralCallback={popup.onNeutralCallback()}>
            <div className="input-wrapper regular-border v-spacer-s">
                <span className="material-symbols-outlined icon input-icon">unsubscribe</span>
                <input placeholder="Deny Reason" value={msg} onChange={(e) => setMsg(e.target.value)}/>
            </div>
        </StandardPopUp>
    )
}
