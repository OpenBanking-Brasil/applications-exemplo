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
          avatar: 'https://cdn.raidiam.io/directory-ui/brand/obbrazil/0.2.0.112/favicon.svg',
          title: 'Mock Bank',
        },
        {
          active: true,
          avatar: 'https://novo.brb.com.br/wp-content/uploads/2021/08/logo.svg',
          title: 'BRB Auth Server 1.0',
        },
        {
          avatar: 'https://bancobari.com.br/assets/openbanking/logo-banco-bari.svg',
          title: 'Banco BARI',
        },
        {
          avatar: 'https://www.original.com.br/img/nav/logo.svg',
          title: 'Banco Original',
        },
      ]
    }),

  }

</script>
