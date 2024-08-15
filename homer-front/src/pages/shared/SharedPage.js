import {useEffect, useState} from "react";
import axios from "axios";
import {API} from "../../environment";
import {NoProperty, PropertyCard, PropertyRequest, SharedPropertyCard} from "../properties/components/PropertyCard";
import {UserPage} from "../root/UserPage";
import CircleIconButton from "../../components/buttons/CircleIconButton";

export default function SharedPage() {
    const token = localStorage.getItem("token")
    const userId = localStorage.getItem("id")
    const [permissions, setPermissions] = useState([])

    useEffect(() => {
        axios.get(API + "/user/" + userId + "/getOwnerPermissions", { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => { 
                setPermissions(response.data)
                console.log(response.data) 
            })
            .catch(e => console.log(e))
    }, [])

    const propertyCallback = (id, userEmail) => {
        let payload = { "propertyId": id, "userEmail": userEmail, "ownerEmail": localStorage.getItem("username"), "deviceId": null }
        console.log(payload)
        axios.post(API + `/user/${localStorage.getItem("id")}/removePropertyPermission`, payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                const filteredPermissions = permissions.filter(item => item.property.id !== id);
                setPermissions(filteredPermissions);
                console.log("PERMISSION REMOVED")
            })
            .catch(e => console.log(e))
    }

    const deviceCallback = (id, userEmail) => {
        let payload = { "propertyId": null, "userEmail": userEmail, "ownerEmail": localStorage.getItem("username"), "deviceId": id }
        console.log(payload)
        axios.post(API + `/user/${localStorage.getItem("id")}/removeDevicePermission`, payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                const filteredPermissions = permissions.filter(item => item.device.id !== id);
                setPermissions(filteredPermissions);
                console.log("PERMISSION REMOVED")
            })
            .catch(e => console.log(e))
    }

    return(
        <UserPage>
            <main className="mh-100">
                <h1 className="page-title">Shared properties</h1>
                <div className="flex wrap gap-xs">
                    {
                        permissions.length > 0 ?
                        permissions.filter((item) => item.property != null).map((item) => 
                            <SharedCard title={item.property.name} sharedWith={item.userEmail} callback={propertyCallback} id={item.property.id}/>
                        ) :
                        <NoSharedProperties/>
                    }
                </div>
                <h1 className="page-title">Shared devices</h1>
                <div className="flex wrap gap-xs">
                    {
                        permissions.length > 0 ?
                        permissions.filter((item) => item.device != null).map((item) => 
                            <SharedCard title={item.device.name} sharedWith={item.userEmail} callback={deviceCallback} id={item.device.id}/>
                        ) :
                        <NoSharedDevices/>
                    }
                </div>
            </main>
        </UserPage>
    )
}

function SharedCard({title, sharedWith, callback, id}) {
    return(
       <div className="card">
            <p className="card-title">{title}</p>
            <div className="data-grid v-spacer-s">
                <p className="key-item">Shared With:</p>
                <p className="value-item">{sharedWith}</p>
            </div>
            <div className="w-100 flex justify-end">
                <button className="text-button" onClick={() => callback(id, sharedWith)}>sToP sHARiNg</button>
            </div>
       </div> 
    )
}

function NoSharedProperties() {
    return(
        <div className="flex center justify-center columns dashed-card v-spacer-l">
            <p className="tutorial-text">No Data</p>
            <p className="card-label">No properties are shared with you</p>
        </div>
    )
}

function NoSharedDevices() {
    return(
        <div className="flex center justify-center columns dashed-card v-spacer-l">
            <p className="tutorial-text">No Data</p>
            <p className="card-label">No devices are shared with you</p>
        </div>
    )
}