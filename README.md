
  
  
### Escuela Colombiana de Ingeniería
### Arquitecturas de Software – ARSW


## Laboratorio – Programación concurrente, condiciones de carrera y sincronización de hilos - Caso Inmortales

### Descripción
Este laboratorio tiene como fin que el estudiante conozca y aplique conceptos propios de la programación concurrente, además de estrategias que eviten condiciones de carrera.
### Dependencias:

* [Ejercicio Introducción al paralelismo - Hilos - BlackList Search](https://github.com/ARSW-ECI-beta/PARALLELISM-JAVA_THREADS-INTRODUCTION_BLACKLISTSEARCH)
#### Parte I – Antes de terminar la clase.

Control de hilos con wait/notify. Productor/consumidor.

1. Revise el funcionamiento del programa y ejecútelo. Mientras esto ocurren, ejecute jVisualVM y revise el consumo de CPU del proceso correspondiente. A qué se debe este consumo?, cual es la clase responsable?

	El alto consumo se debe a que el productor agrega a la cola demasiado lento a comparación del consumidor, lo cual ocaciona que el consumidor busque en la cola más veces
	de las que se necesita lo que aumenta significativamente el consumo de CPU. La clase responsable de esto es Consumer.

	![](./img/rendimientoparte1punto1.PNG)


2. Haga los ajustes necesarios para que la solución use más eficientemente la CPU, teniendo en cuenta que -por ahora- la producción es lenta y el consumo es rápido. Verifique con JVisualVM que el consumo de CPU se reduzca.

	Se modificó el método run de la clase Consumer para que se consuma a la misma velocidad con la que el productor produce:
	```java
	@Override
	public void run() {
	    while (true) {
		if (queue.size() > 0) {
		    int elem=queue.poll();
		    System.out.println("Consumer consumes "+elem);
		}
		try {
		    Thread.sleep(1000);
		} catch (InterruptedException ex) {
		    Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
		}
	    }
	}
	```

	Después de esta modificación se aprecia que el consumo de CPU se reduce significativamente:

	![](./img/rendimientoparte1punto2.png)

3. Haga que ahora el productor produzca muy rápido, y el consumidor consuma lento. Teniendo en cuenta que el productor conoce un límite de Stock (cuantos elementos debería tener, a lo sumo en la cola), haga que dicho límite se respete. Revise el API de la colección usada como cola para ver cómo garantizar que dicho límite no se supere. Verifique que, al poner un límite pequeño para el 'stock', no haya consumo alto de CPU ni errores.

	El ritmo de consumo sigue igual al punto anterior y se aumentó el ritmo de producción:

	```java
	@Override
	    public void run() {
		while (true) {
		    if(queue.size()<stockLimit){
			dataSeed = dataSeed + rand.nextInt(100);
			synchronized (queue){
			    queue.add(dataSeed);
			}
			System.out.println("Producer added " + dataSeed);
		    }
		    try {
			Thread.sleep(100);
		    } catch (InterruptedException ex) {
			Logger.getLogger(Producer.class.getName()).log(Level.SEVERE, null, ex);
		    }
		}
	    }
	```

	Para garantizar el límite de Stock existe un constructor que tiene como parámetro la capacidad máxima de la cola  ```LinkedBlockingQueue(int capacity)``` pero no podemos poner el valor ```Long.MAX_VALUE``` con dicho constructor, por lo que para garantizar que no se exceda este límite se puso la siguiente condición:

	```java
	if(queue.size()<stockLimit){
	    dataSeed = dataSeed + rand.nextInt(100);
	    synchronized (queue){
		queue.add(dataSeed);
	    }
	    System.out.println("Producer added " + dataSeed);
	}
	```

	Rendimiento con la capacidad máxima igual a ```Long.MAX_VALUE```:

	![](./img/rendimientoparte1punto3.png)

	Rendimiento con la capacida máxima igual a 50:

	![](./img/rendimientoparte1punto3capacidadminima.png)

#### Parte II. – Antes de terminar la clase.

Teniendo en cuenta los conceptos vistos de condición de carrera y sincronización, haga una nueva versión -más eficiente- del ejercicio anterior (el buscador de listas negras). En la versión actual, cada hilo se encarga de revisar el host en la totalidad del subconjunto de servidores que le corresponde, de manera que en conjunto se están explorando la totalidad de servidores. Teniendo esto en cuenta, haga que:

- La búsqueda distribuida se detenga (deje de buscar en las listas negras restantes) y retorne la respuesta apenas, en su conjunto, los hilos hayan detectado el número de ocurrencias requerido que determina si un host es confiable o no (_BLACK_LIST_ALARM_COUNT_).

	En lugar de seguir realizando una cuenta separada para cada hilo, se llevó una cuenta en común utilizando una variable AtomicInteger que lleva las ocurrencias de la dirección IP que se buscó. Este tipo de variable es Thread Safety, lo que evita las condiciones de carrera, aumentando el rendimiento de nuestro programa.
	Relizamos las pruebas con una dirección IP muy dispersa, los resultados fueron los siguientes:
	
	Ejecución del código antiguo:
	
	![](./img/pruebaViejoparte2.png)
	
	Ejecución luego de realizar las mejoras:
	
	![](./img/pruebaNuevoparte2.png)


#### Parte II. – Avance para la siguiente clase

Sincronización y Dead-Locks.

![](http://files.explosm.net/comics/Matt/Bummed-forever.png)

1. Revise el programa “highlander-simulator”, dispuesto en el paquete edu.eci.arsw.highlandersim. Este es un juego en el que:

	* Se tienen N jugadores inmortales.
	* Cada jugador conoce a los N-1 jugador restantes.
	* Cada jugador, permanentemente, ataca a algún otro inmortal. El que primero ataca le resta M puntos de vida a su contrincante, y aumenta en esta misma cantidad sus propios puntos de vida.
	* El juego podría nunca tener un único ganador. Lo más probable es que al final sólo queden dos, peleando indefinidamente quitando y sumando puntos de vida.

2. Revise el código e identifique cómo se implemento la funcionalidad antes indicada. Dada la intención del juego, un invariante debería ser que la sumatoria de los puntos de vida de todos los jugadores siempre sea el mismo(claro está, en un instante de tiempo en el que no esté en proceso una operación de incremento/reducción de tiempo). Para este caso, para N jugadores, cual debería ser este valor?.
	
	El valor de la vida total de todos los jugadores debe ser: ```N * DEFAULT_IMMORTAL_HEALTH``` (el valor por defecto de la vida de cada inmortal es 100).

3. Ejecute la aplicación y verifique cómo funcionan las opción ‘pause and check’. Se cumple el invariante?.

	El invariante no se cumple, con 3 inmortales la salud total debería ser de 300, pero hay momentos en los que este valor aumenta indefinidamente como se muestra en la siguiente imagen:
	
	![](./img/errorvidatotalparte3.png)

4. Una primera hipótesis para que se presente la condición de carrera para dicha función (pause and check), es que el programa consulta la lista cuyos valores va a imprimir, a la vez que otros hilos modifican sus valores. Para corregir esto, haga lo que sea necesario para que efectivamente, antes de imprimir los resultados actuales, se pausen todos los demás hilos. Adicionalmente, implemente la opción ‘resume’.

	Para corregir esta condición de carrera primero se pausaron todos los hilos, luego de eso se empezó a realizar la suma para luego imprimir el resultado:
	
	```java
	btnPauseAndCheck.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int sum = 0;
		//Se pausan todos los hilos
                for (Immortal im : immortals) {
                    im.setMovimiento(false);
                }
		//Se realiza la sumatoria de la salud de cada hilo
                for (Immortal im : immortals) {
                    sum += im.getHealth();
                }
                statisticsLabel.setText("<html>"+immortals.toString()+"<br>Health sum:"+ sum);
            }
        });
	```

5. Verifique nuevamente el funcionamiento (haga clic muchas veces en el botón). Se cumple o no el invariante?.
	
	Luego de realizar la corrección sigue sin cumplirse el invariante:
	
	![](./img/invariante1parte3.png)
	![](./img/invariante2parte3.png)
	![](./img/invariante3parte3.png)

6. Identifique posibles regiones críticas en lo que respecta a la pelea de los inmortales. Implemente una estrategia de bloqueo que evite las condiciones de carrera. Recuerde que si usted requiere usar dos o más ‘locks’ simultáneamente, puede usar bloques sincronizados anidados:

	```java
	synchronized(locka){
		synchronized(lockb){
			…
		}
	}
	```
	
	La región crítica que se identificó es el acceso a la población de inmortales por parte de cada inmortal, es región crítica ya que todos acceden y realizan modificaciones a los valores del arreglo de todos los inmortales sin estar sincronizados. Esta región crítica incluye el método fight que es donde se modifican los valores de los inmortales.

7. Tras implementar su estrategia, ponga a correr su programa, y ponga atención a si éste se llega a detener. Si es así, use los programas jps y jstack para identificar por qué el programa se detuvo.
	
	Luego de implementar la estrategía se ejecutó el programa y no presentó ningún error, solo se detiene cuando se cierra la ventana.

8. Plantee una estrategia para corregir el problema antes identificado (puede revisar de nuevo las páginas 206 y 207 de _Java Concurrency in Practice_).

	La estrategía que se planteó es hacer uso de bloques sincronizados anidados, el bloque más grande va a sincronizar el acceso y la toma de datos de la lista de todos los inmortales, y dentro de este bloque irá otro que sincronizará el método fight, para que no haya condiciones de carrera al modificar la vida de los inmortales. El codigo es el siguiente:
	```java
	synchronized (immortalsPopulation){
	    int myIndex = immortalsPopulation.indexOf(this);

	    int nextFighterIndex = r.nextInt(immortalsPopulation.size());

	    //avoid self-fight
	    if (nextFighterIndex == myIndex) {
		nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
	    }
	    im = immortalsPopulation.get(nextFighterIndex);
	    synchronized (im){
		this.fight(im);
	    }
	}
	```

9. Una vez corregido el problema, rectifique que el programa siga funcionando de manera consistente cuando se ejecutan 100, 1000 o 10000 inmortales. Si en estos casos grandes se empieza a incumplir de nuevo el invariante, debe analizar lo realizado en el paso 4.

	El invariante se cumplió al realizar cada una de las pruebas, sin importar el número de inmortales, la suma total de la salud de cada uno de ellos es coherente, a continuación los resultados:

	Prueba con 100 inmortales:

	![](./img/prueba100parte3.png)

	Prueba con 1000 inmortales:

	![](./img/prueba1000parte3.png)

	Prueba con 10000 inmortales:

	![](./img/prueba10000parte3.png)

10. Un elemento molesto para la simulación es que en cierto punto de la misma hay pocos 'inmortales' vivos realizando peleas fallidas con 'inmortales' ya muertos. Es necesario ir suprimiendo los inmortales muertos de la simulación a medida que van muriendo. Para esto:
	* Analizando el esquema de funcionamiento de la simulación, esto podría crear una condición de carrera? Implemente la funcionalidad, ejecute la simulación y observe qué problema se presenta cuando hay muchos 'inmortales' en la misma. Escriba sus conclusiones al respecto en el archivo RESPUESTAS.txt.
	* Corrija el problema anterior __SIN hacer uso de sincronización__, pues volver secuencial el acceso a la lista compartida de inmortales haría extremadamente lenta la simulación.
    
    [Respuestas.txt](/respuesta.txt)
    
11. Para finalizar, implemente la opción STOP.
     ```java
        JButton btnStop = new JButton("STOP");
        btnStop.setForeground(Color.RED);
        toolBar.add(btnStop);
        btnStop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                /**
                 * IMPLEMENTAR
                 */
                for (Immortal im : immortals) {
                    im.setMovimiento(false);
                }
            }
        });
      ```


<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />Este contenido hace parte del curso Arquitecturas de Software del programa de Ingeniería de Sistemas de la Escuela Colombiana de Ingeniería, y está licenciado como <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
