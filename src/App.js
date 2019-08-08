import React, {Component} from 'react';
import Images from './images';

class App extends Component {
  
 render() {
        return (
            <Images images={this.state.images} />
        )
    }

   state = {
        images: []
    };
  
    componentDidMount() {
	    fetch('https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=116867723890a0718ff99f5edb375f1a&text=${term}&format=json&nojsoncallback=1')
            .then(res => res.json())
            .then((data) => {
                 this.setState({ images: data.photos.photo })
            })
            .catch(console.log)
    }
}

export default App;
