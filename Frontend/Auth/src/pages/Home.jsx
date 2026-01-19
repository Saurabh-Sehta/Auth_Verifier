import React from 'react'
import MenuBar from '../components/MenuBar'
import HomeBody from '../components/HomeBody'

const Home = () => {
  return (
    <div className='flex flex-column items-center justify-content-center min-vh-100'>
      <MenuBar />
      <HomeBody />
    </div>
    
  )
}

export default Home