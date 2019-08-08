import React from 'react'

const Images = ({images}) => {
    return (
        <div>
            <center><h1>Images</h1></center>
			<imageSection>
			
            {images.map((image) => (
			    <section>
			  <img src={'https://farm' + image.farm+ '.staticflickr.com/'+image.server+'/'+image.id+'_'+image.secret+'.jpg'}></img>
			   <h4>{image.title}</h4>
			  
			  </section>
            
				
            ))}
			</imageSection>
        </div>
    )
};

export default Images

