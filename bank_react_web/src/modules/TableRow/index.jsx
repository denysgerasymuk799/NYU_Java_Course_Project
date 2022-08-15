/* eslint-disable no-undef */
/* eslint-disable no-console */
import React from 'react'

const TableRow = (obj, key) => {
  return (
    <tr>
        <td>
            <span className="bold-date">{ obj.trans.date }</span>
        </td>
        <td>
            <i className="fa fa-arrow-alt-circle-up" aria-hidden="true"></i>
            <span className="bold-date">
                {(() => {
                    switch (obj.trans.receiverCardId) {
                        case 'TOP_UP':   return ` ${obj.trans.senderCardId}`
                        case localStorage.getItem('card_id'):  return ` ${obj.trans.senderCardId}`
                        default:                 return ` ${obj.trans.receiverCardId}`
                    }
                })()}
            </span>
        </td>
        <td>
            <span className="bold-date">
                {(() => {
                    switch (obj.trans.receiverCardId) {
                        case 'TOP_UP':  return obj.trans.amount;
                        case localStorage.getItem('card_id'):  return obj.trans.amount;
                        default:                 return `-${obj.trans.amount}`;
                    }
                })()}
            </span>.00 
        </td>
        <td className={(() => {
                switch (obj.trans.status) {
                    case 'COMPLETED':   return 'bold-success';
                    default:            return 'bold-error';
                }
            })()}
        >
            <i className="fa fa-circle" aria-hidden="true"></i>
            <span>
                {(() => {
                    switch (obj.trans.status) {
                        case 'COMPLETED':   return ' successful';
                        case 'NEW':         return ' in process';
                        default:            return ' failed';
                    }
                })()}
            </span>
        </td>
    </tr>
  )
}

export default TableRow

