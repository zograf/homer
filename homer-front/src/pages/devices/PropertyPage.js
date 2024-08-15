import {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../environment";
import {NoProperty, PropertyCard, PropertyRequest, SharedPropertyCard} from "../properties/components/PropertyCard";
import {UserPage} from "../root/UserPage";

export default function PropertyPage() {
    const token = localStorage.getItem("token")
    const userId = localStorage.getItem("id")
    const [requests, setRequests] = useState([])
    const [sharedRequests, setSharedRequests] = useState([])
    const [sharedDevices, setSharedDevices] = useState([])

    useEffect(() => {
        axios.get(API + "/property", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { setRequests(response.data)
                console.log(response.data) })
            .catch(e => console.log(e))

        axios.get(API + "/user/" + userId + "/getPermissions", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { 
                setSharedRequests(response.data.properties)
                setSharedDevices(response.data.devices)
                console.log(response.data) })
            .catch(e => console.log(e))
    }, [])

    return(
        <UserPage>
            <main className="mh-100">
                <h1 className="page-title">Owned properties</h1>
                <div className="flex wrap gap-xs">
                    { requests.length > 0 ? requests?.map((item) => {
                        return ( <PropertyCard token={token} property={item} shared={false}/> )
                    }) : <NoProperty/> }
                </div>
                { sharedRequests.length > 0 || sharedDevices.length > 0 ? 
                <>
                <h1 className="page-title">Shared properties</h1>
                <div className="flex wrap gap-xs">
                    { sharedRequests?.map((item) => {
                        return ( <PropertyCard token={token} property={item} shared={true}/> )
                    }) }
                    {
                        sharedDevices.length > 0 ? <SharedPropertyCard token={token}/> : <></>
                    }
                </div></> : <></>
                }
            </main>
        </UserPage>
    )
}