// @flow
import React, { Component } from 'react';
import {
  StyleSheet,
  SafeAreaView,
  View,
  NativeModules,
  Text,
  Image,
  TouchableOpacity
} from 'react-native';

type Props = {};

type State = {
  uri: ?string
};

export default class App extends Component<Props, State> {
  imageView: ?Image;
  state = {};
  onPress = async () => {
    const { RNYuntiImageCropper } = NativeModules;
    const { uri } = Image.resolveAssetSource(require('./demo.png'));
    try {
      const data = await RNYuntiImageCropper.cropWithUri(uri);
      this.setState({ uri: data.uri });
      console.log(data);
    } catch (err) {
      console.log(err);
    }
  };

  render() {
    const { uri } = this.state;
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.imageContainer}>
          <Image
            style={styles.image}
            resizeMode={'contain'}
            source={require('./demo.png')}
            ref={ref => (this.imageView = ref)}
          />
          {uri ? (
            <Image
              style={styles.image}
              resizeMode={'contain'}
              source={{ uri }}
            />
          ) : (
            <View style={styles.image} />
          )}
        </View>

        <TouchableOpacity style={styles.button} onPress={this.onPress}>
          <Text style={{ color: 'white' }}>截图</Text>
        </TouchableOpacity>
      </SafeAreaView>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1
  },
  imageContainer: {
    height: '90%',
    flexDirection: 'row'
  },
  image: {
    flex: 1,
    height: '100%'
  },
  button: {
    alignSelf: 'center',
    padding: 10,
    backgroundColor: '#63B8FF',
    borderRadius: 3
  }
});
