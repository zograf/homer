import "./PopUpFrame.css"
import { useEffect, useRef, useState } from "react";

export const usePopup = () => {
    const [isVisible, setIsVisible] = useState(false)
    const [args, setArgs] = useState(undefined)


    const showPopup = (args) => {
        setIsVisible(true)
        setArgs(args)
    }
    const onPositiveCallback = (positiveCallback) => {
        return () => {
            setIsVisible(false)
            if(positiveCallback) positiveCallback(args)
        }
    }
    const onNeutralCallback = (neutralCallback) => {
        return () => {
            setIsVisible(false)
            if(neutralCallback) neutralCallback(args)
        }
    }

    return {
        isVisible,
        showPopup,
        onPositiveCallback,
        onNeutralCallback,
    }
}

export function PopUpFrame({visible, children}) {
    const dialog = useRef(null)

    useEffect(() => {
        if (dialog.current?.open && !visible) dialog.current?.close()
        else if (!dialog.current?.open && visible) dialog.current?.showModal()
    }, [visible])

    return(
        <dialog className="card" ref={dialog}>
            {children}
        </dialog>
    )
}

export function PopUpPage({visible, children}) {
    const dialog = useRef(null)

    useEffect(() => {
        if (dialog.current?.open && !visible) dialog.current?.close()
        else if (!dialog.current?.open && visible) dialog.current?.showModal()
    }, [visible])

    return(
        <dialog className="page-dialog w-100" ref={dialog}>
            {children}
        </dialog>
    )
}

export function StandardPopUp({visible, title, description, positiveLabel, neutralLabel, positiveCallback, neutralCallback, children}) {
    return(
        <PopUpFrame visible={visible}>
            <div style={{padding: "0 6px"}}>
                <p className="card-title v-spacer-xs" >{title}</p>
                <p className="card-body v-spacer-s">{description}</p>
            </div>
            {children}
            <div className="flex w-100 justify-end center gap-xs">
                <button className="outline-button" onClick={neutralCallback}>{neutralLabel}</button>
                <button className="solid-accent-button" onClick={positiveCallback}>{positiveLabel}</button>
            </div>
        </PopUpFrame>
    )
}