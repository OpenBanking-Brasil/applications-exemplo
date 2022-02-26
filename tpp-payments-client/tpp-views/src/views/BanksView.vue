<template>
  <v-main class="banks">
      <v-row>
      <v-col>
      </v-col>
      <v-col
      :cols="7">

            <v-sheet
              min-height="70vh"
              elevation="20"
              rounded="lg"
            >
            <SheetAppBar header='Mock TPP'/>
              <v-row>
<v-col>
</v-col>
<v-col cols="10">

            <div class="pa-md-4 transition-swing text-h6" align="center"
            >Payment Provider Details
            </div>     
            <v-text-field
            v-model="message4"
            label="Search"
            outlined
            clearable
          ></v-text-field>
          
    
<v-card
    class="mx-auto"
    
  >
 <v-list-item-group
 style="max-height: 300px"
       class="overflow-y-auto"
 
 >
 
      <v-list-item
        v-for="chat in recent"
        :key="chat.title"
        class="pa-md-4"
      >
        <v-list-item-avatar>
          <v-img
            :src="chat.avatar"
          ></v-img>
        </v-list-item-avatar>

        <v-list-item-content>
           
          <v-list-item-title class="text-h5" v-text="chat.title"></v-list-item-title>
         
        </v-list-item-content>
     
     
      </v-list-item>
    </v-list-item-group>
      </v-card>  
      
</v-col>
<v-col>
</v-col>
</v-row>
              <SelectButton/>
            </v-sheet>
      </v-col>
      <v-col>
      </v-col>
      </v-row>

</v-main>
</template>

<script>
// @ is an alias to /src

import SheetAppBar from '@/components/GeneralAppComponents/SheetAppBar.vue'
import SelectButton from '@/components/BankSelect/SelectButton.vue'

export default {
  name: 'BankView',
  components: {
    SheetAppBar,
    SelectButton,
  },
    data: () => ({
      recent: [
        {
          active: true,
          avatar: 'https://cdn.vuetifyjs.com/images/lists/1.jpg',
          title: 'Bank A',
        },
        {
          active: true,
          avatar: 'https://cdn.vuetifyjs.com/images/lists/2.jpg',
          title: 'Bank B',
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/3.jpg',
          title: 'Bank C',
        },
        {
          avatar: 'https://cdn.vuetifyjs.com/images/lists/4.jpg',
          title: 'Bank D',
        },
      ],
      previous: [{
        title: 'Bank E',
        avatar: 'https://cdn.vuetifyjs.com/images/lists/5.jpg',
      }],
    }),

  }

</script>
