## SkinManager
一种整体应用的皮肤切换方案的类库

For more information please see 
[Android上层应用的一种整体应用的皮肤切换方案](https://nesscurie.github.io/2016/12/05/Android%E4%B8%8A%E5%B1%82%E5%BA%94%E7%94%A8%E7%9A%84%E4%B8%80%E7%A7%8D%E6%95%B4%E4%BD%93%E5%BA%94%E7%94%A8%E7%9A%84%E7%9A%AE%E8%82%A4%E5%88%87%E6%8D%A2%E6%96%B9%E6%A1%88/)

## Download
Add this in your root build.gradle file (not your module build.gradle file):
<pre><code>allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
</code></pre>

Then, add the library to your module build.gradle
<pre><code>dependencies {
    implementation 'com.github.NessCurie:SkinManager:latest.release.here'
}
</code></pre>

such as release is v1.0

you can use:
<pre><code>dependencies {
    implementation 'com.github.NessCurie:SkinManager:1.0'
}
</code></pre>