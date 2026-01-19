import React from 'react'
import { assets } from '../assets/assets'

const HomeBody = () => {
  return (
    <div className='text-center d-flex flex-column align-items-center justify-content-center py-5 px-3' style={{minHeight:"80vh"}}>
          <img src={assets.homeBody} alt="header" width={250} className='mb-4' />
          <h5>
            Hey developer! <span role='img' aria-label='wave'>✌️</span>
          </h5>
    
          <h1 className='fw-bold display-5 mb-3'>
            Welcome to our product
          </h1>
    
          <p className='text-muted fs-5 mb-4' style={{maxWidth: "80vw"}}> 
            Let's start with a quick product tour and you can setup the authentication in no time!
          </p>
    
          <button
            type="button"
            className="btn btn-outline-dark rounded-pill px-4 py-2"
          >
            Get Started
          </button>
          
        </div>
  )
}

export default HomeBody