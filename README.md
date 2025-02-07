# Установка Android-версии виджета в мобильное приложение

## Минимальные требования:
1)	Минимальный SDK = 21
2)	Актуальная поддержка SDK = 34

## Установка:

1)	Добавление репозитория для подключения зависимостей

```
dependencyResolutionManagement {
    …
    repositories {
        …
        maven { url 'https://jitpack.io' }
    }
}
```

2)	Добавление зависимостей

    1) image-coil - [![](https://jitpack.io/v/zhombie/garage.svg)](https://jitpack.io/#zhombie/garage)

    ```
    implementation 'com.github.zhombie.garage:image-coil:X.Y.Z'
    ```

    2) qbox-widget-webview-android - [![](https://jitpack.io/v/zhombie/qbox-widget-webview-android.svg)](https://jitpack.io/#zhombie/qbox-widget-webview-android)
    
    ```
    implementation 'com.github.zhombie:qbox-widget-webview-android:X.Y.Z'
    ```

3)	Определение загрузчика рисунков (можно указать свой вариант. Например, библиотека Glide от Google. В таком случае требуется реализация интерфейса ImageLoader и переопределения его методов)

```
class Application : Application(), ImageLoaderFactory {

    override fun getImageLoader(): ImageLoader =
        CoilImageLoader(
            context = this,
            allowHardware = true,
            crossfade = false,
            isLoggingEnabled = false
        )

}
```

4)	Описание путей для хранения скачанных файлов. При наличии у приложения уже описанной FileProvider, то можно пропустить данный шаг, главное, чтобы совпадали authorities как указано внизу

```
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.provider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

5)	Запуск виджета. Есть 2 вида запуска виджета:

    1) WebViewActivity
    2) Кастомное Activity

#### С использованием WebViewActivity:

Для этого понадобится просто вызвать Builder класс и передать нужные параметры и вызвать метод launch(), ниже приведен пример:

```
Widget.Builder.VideoCall(this)
                .setLoggingEnabled(true)
                .setUrl(params.url)
                .setLanguage(Language.KAZAKH)
                .setCall(call = params.call)
                .setUser(exampleCustomer)
                .launch()
```

#### С использованием кастомного Activity:

* Для начала в xml файле вашего Activity пропишите FragmentContainerView:

```
<androidx.fragment.app.FragmentContainerView
        android:id="@+id/fragmentContainerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
```

* Вам нужно инициализировать его в вашем Activity
* Необходимо также инициализировать такие параметры как flavor, url, language, call, user, dynamicAttrs, для передачи данных. А также callback (для отправкий событий в WebViewFragment). Ниже приведен пример:

  ```
  private val language by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("language") ?: Locale.getDefault().language
    }

    private val flavor by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getEnum<Flavor>(intent, "flavor") ?: throw IllegalStateException()
    }

    private val url by lazy(LazyThreadSafetyMode.NONE) {
        intent.getStringExtra("url") ?: throw IllegalStateException()
    }

    private val call by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<Call>(intent, "call")
    }

    private val user by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<User>(intent, "user")
    }

    private val dynamicAttrs by lazy(LazyThreadSafetyMode.NONE) {
        IntentCompat.getSerializable<DynamicAttrs>(intent, "dynamic_attrs")
    }

    private var callback: Callback? = null
  ```

* Далее вам нужно будет переопределить методы onBackPressed и onUserLeaveHint

  ```
  @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        callback?.onBackPressed { super.onBackPressed() }
    }

    override fun onUserLeaveHint() {
        callback?.onUserLeaveHint { super.onUserLeaveHint() }
    }
  ```

* Затем надо сделать тоже самое с методом onPictureInPictureModeChanged, в этом методе вы, исходя из того находится ли ваше приложение в режиме "PictureInPicture" или нет скрываете или же на оборот показываете весь остальной контент помимо FragmentContainerView

  ```
  override fun onPictureInPictureModeChanged(
          isInPictureInPictureMode: Boolean, newConfig: Configuration
      ) {
          if (isInPictureInPictureMode) {
              contentView?.visibility = View.GONE
          } else {
              contentView?.visibility = View.VISIBLE
          }
          super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
      }
  ```

6)	Для управления глобальными значениями предоставляется Singleton объект Widget

```
Widget.isLoggingEnabled = true
```

Например, можно при желании включать/отключать логирование

## Важное примечание (если вы используете кастомное Activity):

* Callback имеет метод onReload, его вы можете использовать по своему желанию, он перезагружает WebView в WebViewFragment
* Для корректной работы PictureInPicture в вашем Activity, в AndroidManifest необходимо прописать такие параметры как:

  ```
  android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"
  android:supportsPictureInPicture="true"
  ```

## Пример:

В модуле [sample](sample) есть пример работы с библиотекой
