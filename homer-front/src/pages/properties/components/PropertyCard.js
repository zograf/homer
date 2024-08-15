import { useState, useEffect } from 'react'
import './PropertyCard.css'
import axios from 'axios'
import {API, IMG} from "../../../environment";
import CircleIconButton from '../../../components/buttons/CircleIconButton';
import { StandardPopUp, usePopup } from '../../../components/popup/PopUpFrame';

export function PropertyRequest({token, property, approveCallback, denyCallback}) {
    const [imageLarge, setImageLarge] = useState(null)

    useEffect(() => {
        axios.get(IMG + `/property-${property.id}`, { headers: { "Authorization": `Bearer ${token}` }, responseType: "blob" })
            .then(response => {
                setImageLarge(response.data)
            })
            .catch(e => console.log(e))
    }, []);

    return(
        <div className="card property-wrapper showing">
            <div className="property-img-wrapper p-img-large h-spacer-s flex justify-center">
                <img
                    className="p-img-large"
                    alt="not found"
                    src={imageLarge == null ? require('../../../img/default.png') : URL.createObjectURL(imageLarge)}
                />
            </div>
            <div className="h-spacer-xs">
                <p className="card-title">{property.name}</p>
                <div className="data-grid v-spacer-s">
                    <p className="key-item">Country:</p>
                    <p className="value-item">{property.country.name}</p>

                    <p className="key-item">Town:</p>
                    <p className="value-item">{property.city.name}</p>

                    <p className="key-item">Address:</p>
                    <p className="value-item">{property.street}</p>

                    <p className="key-item">Area:</p>
                    <p className="value-item">{property.area}m2</p>

                    <p className="key-item">Floors:</p>
                    <p className="value-item">{property.floors}</p>
                </div>
                <div className='flex gap-xs justify-end'>
                    <button className='small-button solid-accent-button' onClick={(e) => { approveCallback(property.id) }}>Approve</button>
                    <button className='small-button outline-button' onClick={(e) => { denyCallback(property.id) }}>Deny</button>
                </div>
            </div>
        </div>
    )
}

export function PropertyCard({token, property, shared}) {
    const [imageSmall, setImageSmall] = useState(null)
    const handleClick = () => { window.location.href = "/devices/" + property.id + "/" + shared}

    useEffect(() => {
        axios.get(IMG + `/property-${property.id}`, { headers: { "Authorization": `Bearer ${token}` }, responseType: "blob" })
            .then(response => {
                setImageSmall(response.data)
            })
            .catch(e => console.log(e))
    }, []);

    const sharePopup = usePopup()
    const handleShare = (id, email) => {
        // PropertyId, Email
        console.log(id, email)
        let payload = { "propertyId": id, "userEmail": email, "ownerEmail": localStorage.getItem("username"), "deviceId": null }
        axios.post(API + `/user/${localStorage.getItem("id")}/propertyPermission`, payload, { headers: {"Authorization" : `Bearer ${token}`} })
            .then(response => {
                console.log("PERMISSION GRANTED")
            })
            .catch(e => console.log(e))
    }

    const share = () => {
        sharePopup.showPopup(property.id)
    }

    return(
        <div className="card property-wrapper showing">
            <div className="property-img-wrapper p-img-small h-spacer-s flex justify-center">
                <img
                    className="p-img-small"
                    alt="not found"
                    src={imageSmall == null ? require('../../../img/default.png') : URL.createObjectURL(imageSmall)}
                />
            </div>
            <div className={['h-spacer-xs', property.status === "REQUESTED" ? 'neutral' : 'normal'].join(' ')}>

                <p className="card-title">{property.name}</p>
                <div className="data-grid v-spacer-s">
                    <p className="key-item">Country:</p>
                    <p className="value-item">{property.country.name}</p>

                    <p className="key-item">Town:</p>
                    <p className="value-item">{property.city.name}</p>

                    <p className="key-item">Address:</p>
                    <p className="value-item">{property.street}</p>

                    <p className="key-item">Area:</p>
                    <p className="value-item">{property.area}m2</p>

                    <p className="key-item">Floors:</p>
                    <p className="value-item">{property.floors}</p>

                    <p className="key-item normal">Status:</p>
                    <p className={['value-item', property.status === "REQUESTED" ? 'normal' : 'accent'].join(' ')}>{property.status === "REQUESTED" ? 'Requested' : 'Active'}</p>

                </div>
                <div className='flex gap-xs justify-end center'>
                    {
                        shared ? <></> : property.status === "ACCEPTED" ? <CircleIconButton icon={"share"} onClick={share}/> : <></>
                    }
                    <button disabled={property.status !== "ACCEPTED"} className={["small-button", property.status === "ACCEPTED" ? "solid-button" : "disabled-solid-button"].join(" ")} onClick={handleClick}>View Devices</button>
                    <SharePopUp popup={sharePopup} callback={handleShare}/>
                </div>
            </div>
        </div>
    )
}

export function NoProperty() {
    return(
        <div className="dashed-container flex center justify-center columns gap-s showing">
            <p className="tutorial-text">You don't have owned properties.</p>
            <button className="solid-button" onClick={() => { window.location.href = '/property/add'}}>Add Property</button>
        </div>
    )
}

export function NoPropertyRequests() {
    return(
        <div className="dashed-container flex center justify-center columns gap-s showing">
            <p className="tutorial-text">You don't have any property requests.</p>
            <button className="outline-button" onClick={() => { window.location.reload()}}>Refresh</button>
        </div>
    )
}

function SharePopUp({popup, callback}) {

    const [msg, setMsg] = useState("")
    const handleDenyPopUp = (id) => {
        callback(id, msg)
        setMsg("")
    }

    return(
        <StandardPopUp
            visible={popup.isVisible}
            title={"Share property"}
            description={"Please enter the user's email to share the property"}
            neutralLabel={"Cancel"}
            positiveLabel={"Share"}
            positiveCallback={popup.onPositiveCallback(handleDenyPopUp)}
            neutralCallback={popup.onNeutralCallback()}>
            <div className="input-wrapper regular-border v-spacer-s">
                <span className="material-symbols-outlined icon input-icon">email</span>
                <input placeholder="Email" value={msg} onChange={(e) => setMsg(e.target.value)}/>
            </div>
        </StandardPopUp>
    )
}

export function SharedPropertyCard({token}) {
    const handleClick = () => { window.location.href = "/devices/0/true"}

    return(
        <div className="card property-wrapper showing">
            <div className={['h-spacer-xs', 'normal'].join(' ')}>
                <p className="card-title v-spacer-s">Shared devices</p>

                <div className='flex gap-xs justify-end center'>
                    <button className={["small-button", "solid-button"].join(" ")} onClick={handleClick}>View shared devices</button>
                </div>
            </div>
        </div>
    )
}