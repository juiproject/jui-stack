Interop = function () {}

Interop.prototype.button1 = function() {
  window.alert ("Button 1 pressed");
}

Interop.prototype.button2 = function() {
  var obj = new com.effacy.jui.playground.ui.samples.InteropExported ();
  window.alert (obj.message ());
}

Interop.prototype.button3 = function() {
  var api = new com.effacy.jui.playground.ui.samples.InteropExported ();
  var query = new com.effacy.jui.playground.ui.samples.InteropExported.Query ("some content");
  api.query (query, (v) => window.alert (v.getName ()));
}

Interop.prototype.attach = function(elementId) {
  new Vue({
    el: '#' + elementId,
    data: {
      players: [
        { id: "1", 
          name: "Lionel Messi", 
          description: "Argentina's superstar" },
        { id: "2", 
          name: "Christiano Ronaldo", 
          description: "World #1-ranked player from Portugal" }
      ]
    }
  });
}

Vue.component('player-card', {
  props: ['player'],
  template: `<div class="card">
      <div class="card-body">
          <h6 class="card-title">
              {{ player.name }}
          </h6>
          <p class="card-text">
            <div>
              {{ player.description }}
            </div>
          </p>
        </div>
      </div>`
});